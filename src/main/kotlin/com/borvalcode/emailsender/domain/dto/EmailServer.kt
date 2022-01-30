package com.borvalcode.emailsender.domain.dto


internal data class EmailServer (val host: String, val port: Int){
    companion object {
        private val PATTERN = "^\\s*(.*?):(\\d+)\\s*$".toRegex()

        @JvmStatic
        infix fun of(url: String)  =
            if (url matches  PATTERN) {
                val match = PATTERN.matchEntire(url)
                EmailServer(match!!.groups[1]!!.value, match.groups[2]!!.value.toInt())
            } else {
                throw IllegalArgumentException("Pattern must be [host]:[port], wrong $url")
            }
    }

}