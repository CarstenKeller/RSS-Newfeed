package com.carstenkeller.rssnewfeed.data.network

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class ParsedFeed(
    val title: String,
    val language: String?,
    val items: List<ParsedItem>,
)

data class ParsedItem(
    val guid: String,
    val title: String,
    val summary: String,
    val contentHtml: String?,
    val imageUrl: String?,
    val link: String,
    val publishedAt: Long?,
    val categories: List<String> = emptyList(),
)

/**
 * Minimal RSS 2.0 / Atom parser built on the platform XmlPullParser so no extra
 * dependency is needed. Unknown elements/namespaces are simply skipped.
 */
object RssParser {

    fun parse(input: InputStream): ParsedFeed {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, null)

        var feedTitle = ""
        var feedLanguage: String? = null
        val items = mutableListOf<ParsedItem>()

        // Item/entry subtrees are fully consumed by parseRssItem/parseAtomEntry below and
        // never seen here, so the first <title>/<language> this loop encounters is always
        // the channel's (RSS) or feed's (Atom) own — no need to hard-code a nesting depth,
        // which differs between RSS (channel > title, depth 3) and Atom (feed > title, depth 2).
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (localName(parser.name)) {
                    "item" -> items += parseRssItem(parser)
                    "entry" -> items += parseAtomEntry(parser)
                    "title" -> if (feedTitle.isEmpty()) feedTitle = readText(parser)
                    "language" -> if (feedLanguage == null) feedLanguage = readText(parser)
                }
            }
            eventType = parser.next()
        }

        return ParsedFeed(title = feedTitle, language = feedLanguage, items = items)
    }

    private fun parseRssItem(parser: XmlPullParser): ParsedItem {
        var guid: String? = null
        var title = ""
        var description = ""
        var contentEncoded: String? = null
        var link = ""
        var imageUrl: String? = null
        var pubDate: Long? = null
        val categories = mutableListOf<String>()

        val startDepth = parser.depth
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.depth == startDepth)) {
            if (eventType == XmlPullParser.START_TAG) {
                when (localName(parser.name)) {
                    "guid" -> guid = readText(parser)
                    "title" -> title = readText(parser)
                    "description" -> description = readText(parser)
                    "encoded" -> contentEncoded = readText(parser)
                    "link" -> link = readText(parser)
                    "pubDate" -> pubDate = parseRfc822Date(readText(parser))
                    "category" -> readText(parser).takeIf { it.isNotBlank() }?.let(categories::add)
                    "enclosure" -> {
                        val type = parser.getAttributeValue(null, "type") ?: ""
                        val url = parser.getAttributeValue(null, "url")
                        if (imageUrl == null && type.startsWith("image") && url != null) imageUrl = url
                    }
                    "thumbnail" -> {
                        val url = parser.getAttributeValue(null, "url")
                        if (imageUrl == null && url != null) imageUrl = url
                    }
                    "content" -> {
                        val url = parser.getAttributeValue(null, "url")
                        val medium = parser.getAttributeValue(null, "medium")
                        if (imageUrl == null && (medium == "image" || url?.let { isImageUrl(it) } == true) && url != null) {
                            imageUrl = url
                        }
                    }
                    else -> skipCurrentTag(parser)
                }
            }
            eventType = parser.next()
        }

        val resolvedImage = imageUrl ?: extractFirstImageUrl(contentEncoded ?: description)
        return ParsedItem(
            guid = guid ?: link,
            title = title,
            summary = htmlToPlainText(description).take(400),
            contentHtml = contentEncoded,
            imageUrl = resolvedImage,
            link = link,
            publishedAt = pubDate,
            categories = categories,
        )
    }

    private fun parseAtomEntry(parser: XmlPullParser): ParsedItem {
        var id: String? = null
        var title = ""
        var summary = ""
        var content: String? = null
        var link = ""
        var published: Long? = null
        val categories = mutableListOf<String>()

        val startDepth = parser.depth
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.depth == startDepth)) {
            if (eventType == XmlPullParser.START_TAG) {
                when (localName(parser.name)) {
                    "id" -> id = readText(parser)
                    "title" -> title = readText(parser)
                    "summary" -> summary = readText(parser)
                    "content" -> content = readText(parser)
                    "link" -> {
                        val href = parser.getAttributeValue(null, "href")
                        val rel = parser.getAttributeValue(null, "rel")
                        if (href != null && (rel == null || rel == "alternate")) link = href
                        skipCurrentTag(parser)
                    }
                    "category" -> {
                        parser.getAttributeValue(null, "term")?.takeIf { it.isNotBlank() }?.let(categories::add)
                        skipCurrentTag(parser)
                    }
                    "published", "updated" -> {
                        val text = readText(parser)
                        if (published == null) published = parseIsoDate(text)
                    }
                    else -> skipCurrentTag(parser)
                }
            }
            eventType = parser.next()
        }

        val body = content ?: summary
        return ParsedItem(
            guid = id ?: link,
            title = title,
            summary = htmlToPlainText(summary.ifEmpty { body }).take(400),
            contentHtml = content,
            imageUrl = extractFirstImageUrl(body),
            link = link,
            publishedAt = published,
            categories = categories,
        )
    }

    private fun localName(name: String): String = name.substringAfterLast(':')

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text ?: ""
            parser.nextTag()
        }
        return result.trim()
    }

    private fun skipCurrentTag(parser: XmlPullParser) {
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
            }
        }
    }

    private fun isImageUrl(url: String): Boolean =
        Regex(".*\\.(jpg|jpeg|png|gif|webp)$", RegexOption.IGNORE_CASE).matches(url)

    private val IMG_SRC_REGEX = Regex("<img[^>]+src=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)

    private fun extractFirstImageUrl(html: String?): String? =
        html?.let { IMG_SRC_REGEX.find(it)?.groupValues?.get(1) }

    private fun htmlToPlainText(html: String): String =
        html.replace(Regex("<[^>]*>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun parseRfc822Date(text: String): Long? = try {
        OffsetDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
    } catch (e: DateTimeParseException) {
        null
    }

    private fun parseIsoDate(text: String): Long? = try {
        Instant.parse(text).toEpochMilli()
    } catch (e: DateTimeParseException) {
        try {
            OffsetDateTime.parse(text).toInstant().toEpochMilli()
        } catch (e2: DateTimeParseException) {
            null
        }
    }
}
