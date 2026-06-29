package com.learner.lm.ai

class SubjectClassifier {

    private val mathKeywords = setOf(
        "equation", "solve", "algebra", "geometry", "fraction", "percent",
        "multiply", "divide", "add", "subtract", "variable", "graph", "theorem"
    )
    private val scienceKeywords = setOf(
        "atom", "molecule", "cell", "photosynthesis", "gravity", "force",
        "energy", "chemical", "biology", "physics", "experiment", "hypothesis"
    )
    private val englishKeywords = setOf(
        "essay", "grammar", "verb", "noun", "paragraph", "thesis", "literature",
        "poem", "writing", "sentence", "punctuation", "analyze", "theme"
    )
    private val historyKeywords = setOf(
        "war", "revolution", "empire", "century", "president", "civilization",
        "timeline", "ancient", "colony", "treaty", "dynasty"
    )
    private val geographyKeywords = setOf(
        "continent", "country", "climate", "map", "latitude", "longitude",
        "river", "mountain", "population", "region", "ecosystem"
    )

    fun classify(text: String): Subject {
        val normalized = text.lowercase()
        val scores = mapOf(
            Subject.MATH to countMatches(normalized, mathKeywords),
            Subject.SCIENCE to countMatches(normalized, scienceKeywords),
            Subject.ENGLISH to countMatches(normalized, englishKeywords),
            Subject.HISTORY to countMatches(normalized, historyKeywords),
            Subject.GEOGRAPHY to countMatches(normalized, geographyKeywords)
        )
        val best = scores.maxByOrNull { it.value }
        return if (best == null || best.value == 0) Subject.GENERAL else best.key
    }

    private fun countMatches(text: String, keywords: Set<String>): Int =
        keywords.count { keyword -> text.contains(keyword) }
}
