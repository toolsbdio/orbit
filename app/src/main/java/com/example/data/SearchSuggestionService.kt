package com.example.data

import com.example.model.SearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

open class SearchSuggestionService {
    open suspend fun fetchSuggestions(
        query: String,
        engine: SearchEngine,
    ): List<String> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        val encoded = URLEncoder.encode(trimmed, "UTF-8")
        val endpoint = suggestionUrlFor(encoded, engine)

        runCatching {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "OrbitBrowser/1.0 (Android)")
            }
            try {
                if (connection.responseCode !in 200..299) {
                    return@runCatching emptyList()
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseOpenSearchJson(body)
            } finally {
                connection.disconnect()
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        fun suggestionUrlFor(encodedQuery: String, engine: SearchEngine): String {
            return when (engine) {
                SearchEngine.DUCKDUCKGO -> "https://duckduckgo.com/ac/?q=$encodedQuery&type=list"
                SearchEngine.GOOGLE -> "https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery"
                SearchEngine.BING -> "https://api.bing.com/osjson.aspx?query=$encodedQuery"
                SearchEngine.BRAVE -> "https://search.brave.com/api/suggest?q=$encodedQuery"
            }
        }

        fun parseOpenSearchJson(body: String): List<String> {
            val trimmed = body.trim()
            if (!trimmed.startsWith("[")) return emptyList()

            val strings = mutableListOf<String>()
            var depth = 0
            var inString = false
            var escaped = false
            var insideObject = false
            var inObjectKey = false
            var expectPhraseValue = false
            val current = StringBuilder()
            var stringDepth = 0

            for (i in trimmed.indices) {
                val ch = trimmed[i]
                if (inString) {
                    if (escaped) {
                        when (ch) {
                            '"' -> current.append('"')
                            '\\' -> current.append('\\')
                            '/' -> current.append('/')
                            'n' -> current.append(' ')
                            't' -> current.append(' ')
                            else -> current.append(ch)
                        }
                        escaped = false
                    } else if (ch == '\\') {
                        escaped = true
                    } else if (ch == '"') {
                        inString = false
                        val token = current.toString().trim()
                        current.setLength(0)

                        if (stringDepth == 2) {
                            if (!insideObject) {
                                if (token.isNotEmpty() && token !in strings) {
                                    strings.add(token)
                                }
                            } else if (inObjectKey) {
                                expectPhraseValue = (token == "phrase")
                                inObjectKey = false
                            } else if (expectPhraseValue) {
                                if (token.isNotEmpty() && token !in strings) {
                                    strings.add(token)
                                }
                                expectPhraseValue = false
                            }
                        }
                        if (strings.size >= 5) break
                    } else {
                        current.append(ch)
                    }
                } else {
                    when (ch) {
                        '[' -> depth++
                        '{' -> {
                            depth++
                            if (depth == 2) {
                                insideObject = true
                                inObjectKey = true
                                expectPhraseValue = false
                            }
                        }
                        ']' -> depth--
                        '}' -> {
                            if (depth == 2) {
                                insideObject = false
                            }
                            depth--
                        }
                        ',' -> {
                            if (depth == 2 && insideObject && !expectPhraseValue) {
                                inObjectKey = true
                            }
                        }
                        ':' -> inObjectKey = false
                        '"' -> {
                            inString = true
                            stringDepth = depth
                            current.setLength(0)
                        }
                    }
                }
            }
            return strings
        }
    }
}
