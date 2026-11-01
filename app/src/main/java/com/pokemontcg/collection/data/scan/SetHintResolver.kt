package com.pokemontcg.collection.data.scan

object SetHintResolver {
    private val mark30C = Regex("""\b30\s*[Cc]\b""")
    private val mark30th = Regex("""\b30\s*(?:th|\.)\b|30th|celebration""", RegexOption.IGNORE_CASE)

    fun extractSetIds(text: String): List<String> {
        val hints = linkedSetOf<String>()
        if (mark30C.containsMatchIn(text) || mark30th.containsMatchIn(text)) {
            hints.add("30th")
        }
        return hints.toList()
    }

    fun tcgDexCardId(setId: String, localId: String): String = "$setId-$localId"
}
