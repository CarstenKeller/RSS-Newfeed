package com.carstenkeller.rssnewfeed.domain

/**
 * Heuristic German keyword list per topic, used only as a fallback signal when a feed
 * has no assigned topic and the item carries no matching `<category>` tag. This is a
 * simple substring/keyword match, not real classification — it will miss articles and
 * occasionally misfire on ambiguous words. Deliberate scope choice (see PLAN.md).
 */
private val TOPIC_KEYWORDS: Map<String, List<String>> = mapOf(
    "Politik" to listOf(
        "bundestag", "bundesrat", "wahl", "kanzler", "minister", "ministerin", "partei",
        "koalition", "regierung", "opposition", "landtag", "spd", "cdu", "csu", "grüne",
        "fdp", "afd", "linke", "bsw", "gesetzentwurf", "abgeordnete",
    ),
    "Weltgeschehen" to listOf(
        "krieg", "ukraine", "russland", "nato", "uno", "vereinte nationen", "gaza",
        "israel", "krise", "konflikt", "gipfel", "sanktionen", "außenpolitik", "flüchtlinge",
    ),
    "Wirtschaft" to listOf(
        "inflation", "dax", "aktie", "börse", "zins", "ezb", "wirtschaft", "unternehmen",
        "konjunktur", "arbeitsmarkt", "export", "insolvenz", "gewinn", "umsatz", "konzern",
    ),
    "Sport" to listOf(
        "bundesliga", "fußball", "champions league", "olympia", "weltmeisterschaft", "em ",
        "tor", "spieler", "verein", "meisterschaft", "trainer", "turnier",
    ),
    "Kultur" to listOf(
        "film", "kino", "musik", "konzert", "ausstellung", "buch", "theater", "festival",
        "kultur", "album", "regisseur", "schauspieler",
    ),
    "Wissenschaft & Technik" to listOf(
        "studie", "forscher", "wissenschaft", "technologie", "künstliche intelligenz", " ki ",
        "raumfahrt", "klima", "forschung", "smartphone", "software", "roboter",
    ),
    "Auto" to listOf(
        "auto", "autos", "pkw", "fahrzeug", "elektroauto", "e-auto", "verbrenner",
        "hersteller", "modell", "suv", "hybrid", "tesla", "vw", "volkswagen", "bmw",
        "mercedes", "audi", "porsche", "test drive", "motor", "reifen", "ladesäule",
    ),
)

object TopicMatching {

    fun matches(
        feedTopicTag: String?,
        categoriesCsv: String,
        title: String,
        summary: String,
        watchedTopics: Set<String>,
    ): Boolean {
        if (feedTopicTag != null && feedTopicTag in watchedTopics) return true

        if (categoriesCsv.isNotBlank()) {
            val categories = categoriesCsv.split(",")
            if (watchedTopics.any { topic -> categories.any { it.contains(topic, ignoreCase = true) } }) {
                return true
            }
        }

        val text = " ${title.lowercase()} ${summary.lowercase()} "
        return watchedTopics.any { topic ->
            TOPIC_KEYWORDS[topic]?.any { keyword -> text.contains(keyword.lowercase()) } == true
        }
    }
}
