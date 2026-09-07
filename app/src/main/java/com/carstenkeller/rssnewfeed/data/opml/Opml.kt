package com.carstenkeller.rssnewfeed.data.opml

import android.util.Xml
import com.carstenkeller.rssnewfeed.data.db.FeedEntity
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.io.StringWriter

data class OpmlFeed(val title: String, val url: String, val topicTag: String?)

/** Writes/reads the standard OPML subscription-list format so feed lists survive outside the app. */
object Opml {

    fun write(feeds: List<FeedEntity>): String {
        val writer = StringWriter()
        val serializer = Xml.newSerializer()
        serializer.setOutput(writer)
        serializer.startDocument("UTF-8", true)
        serializer.startTag(null, "opml")
        serializer.attribute(null, "version", "2.0")
        serializer.startTag(null, "head")
        serializer.startTag(null, "title")
        serializer.text("RSS Newsfeed – Feeds")
        serializer.endTag(null, "title")
        serializer.endTag(null, "head")
        serializer.startTag(null, "body")
        for (feed in feeds) {
            serializer.startTag(null, "outline")
            serializer.attribute(null, "text", feed.title)
            serializer.attribute(null, "title", feed.title)
            serializer.attribute(null, "type", "rss")
            serializer.attribute(null, "xmlUrl", feed.url)
            feed.language?.let { serializer.attribute(null, "language", it) }
            feed.topicTag?.let { serializer.attribute(null, "category", it) }
            serializer.endTag(null, "outline")
        }
        serializer.endTag(null, "body")
        serializer.endTag(null, "opml")
        serializer.endDocument()
        return writer.toString()
    }

    fun parse(input: InputStream): List<OpmlFeed> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, null)

        val feeds = mutableListOf<OpmlFeed>()
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "outline") {
                val url = parser.getAttributeValue(null, "xmlUrl")
                if (url != null) {
                    val title = parser.getAttributeValue(null, "title")
                        ?: parser.getAttributeValue(null, "text")
                        ?: url
                    val category = parser.getAttributeValue(null, "category")
                    feeds += OpmlFeed(title = title, url = url, topicTag = category)
                }
            }
            eventType = parser.next()
        }
        return feeds
    }
}
