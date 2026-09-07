package com.carstenkeller.rssnewfeed.domain

/**
 * Free-text "Suchbegriffe": matches a term against title, summary, and `<category>` tags
 * together, since none of these are a controlled vocabulary - a category tag is just as
 * unstandardized as article text, so there's no reason to treat it differently.
 */
object SearchTermMatching {
    fun matchesAny(terms: Set<String>, title: String, summary: String, categoriesCsv: String): Boolean {
        if (terms.isEmpty()) return false
        val haystack = "$title $summary $categoriesCsv".lowercase()
        return terms.any { haystack.contains(it.lowercase()) }
    }
}
