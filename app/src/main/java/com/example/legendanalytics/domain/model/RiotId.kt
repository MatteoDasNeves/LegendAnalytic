package com.example.legendanalytics.domain.model

data class RiotId(val gameName: String, val tagLine: String) {

    override fun toString(): String = "$gameName#$tagLine"

    companion object {
        private const val MAX_GAME_NAME = 16
        private val TAG_REGEX = Regex("^[\\p{L}\\p{N}]{3,5}$")

        /** Parse "GameName#TAG". Renvoie null si le format est invalide. */
        fun parse(raw: String): RiotId? {
            val input = raw.trim()
            val separator = input.lastIndexOf('#')
            if (separator <= 0 || separator == input.lastIndex) return null
            val gameName = input.substring(0, separator).trim()
            val tagLine = input.substring(separator + 1).trim()
            if (gameName.isEmpty() || gameName.length > MAX_GAME_NAME) return null
            if (!TAG_REGEX.matches(tagLine)) return null
            return RiotId(gameName, tagLine)
        }
    }
}
