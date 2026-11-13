package com.example

import com.example.data.SearchSuggestionService
import com.example.model.BrandIconLookup
import com.example.model.SearchEngine
import com.example.model.UrlNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun urlNormalizerHandlesDomainsSchemesAndSearchQueries() {
        assertEquals(
            "https://wikipedia.org",
            UrlNormalizer.resolveInput("wikipedia.org", SearchEngine.DUCKDUCKGO),
        )
        assertEquals(
            "https://github.com/trending",
            UrlNormalizer.resolveInput("https://github.com/trending", SearchEngine.GOOGLE),
        )
        assertEquals(
            "http://localhost:8080/test",
            UrlNormalizer.resolveInput("localhost:8080/test", SearchEngine.BING),
        )
        assertEquals(
            "https://duckduckgo.com/?q=kotlin+compose",
            UrlNormalizer.resolveInput("kotlin compose", SearchEngine.DUCKDUCKGO),
        )
        assertEquals(
            "https://www.google.com/search?q=android+browser",
            UrlNormalizer.resolveInput("android browser", SearchEngine.GOOGLE),
        )
    }

    @Test
    fun isLikelyUrlOrDomainDistinguishesDomainsFromSearchQueries() {
        assertTrue(UrlNormalizer.isLikelyUrlOrDomain("wikipedia.org"))
        assertTrue(UrlNormalizer.isLikelyUrlOrDomain("https://developer.android.com"))
        assertTrue(UrlNormalizer.isLikelyUrlOrDomain("localhost:3000"))
        assertFalse(UrlNormalizer.isLikelyUrlOrDomain("kotlin"))
        assertFalse(UrlNormalizer.isLikelyUrlOrDomain("weather in tokyo"))
    }

    @Test
    fun openSearchParserHandlesStandardAndPhraseFormats() {
        val openSearchBody = """["kotlin",["kotlin coroutines","kotlin flow","kotlin compose"]]"""
        assertEquals(
            listOf("kotlin coroutines", "kotlin flow", "kotlin compose"),
            SearchSuggestionService.parseOpenSearchJson(openSearchBody),
        )

        val phraseBody = """[{"phrase":"android"},{"phrase":"android auto"}]"""
        assertEquals(
            listOf("android", "android auto"),
            SearchSuggestionService.parseOpenSearchJson(phraseBody),
        )
    }

    @Test
    fun searchSuggestionUrlMatchesSelectedEngine() {
        assertTrue(
            SearchSuggestionService.suggestionUrlFor("compose", SearchEngine.GOOGLE)
                .contains("suggestqueries.google.com"),
        )
        assertTrue(
            SearchSuggestionService.suggestionUrlFor("compose", SearchEngine.DUCKDUCKGO)
                .contains("duckduckgo.com/ac/"),
        )
        assertTrue(
            SearchSuggestionService.suggestionUrlFor("compose", SearchEngine.BING)
                .contains("api.bing.com"),
        )
        assertTrue(
            SearchSuggestionService.suggestionUrlFor("compose", SearchEngine.BRAVE)
                .contains("search.brave.com/api/suggest"),
        )
    }

    @Test
    fun extractHostStripsWwwPrefix() {
        assertEquals(
            "wikipedia.org",
            UrlNormalizer.extractHost("https://www.wikipedia.org/wiki/Android"),
        )
    }

    @Test
    fun brandIconLookupMapsKnownDomainsToSimpleIcons() {
        assertEquals(R.drawable.ic_brand_github, BrandIconLookup.iconResForUrl("https://github.com/simple-icons/simple-icons"))
        assertEquals(R.drawable.ic_brand_wikipedia, BrandIconLookup.iconResForUrl("https://en.m.wikipedia.org"))
        assertEquals(null, BrandIconLookup.iconResForUrl("https://example.org"))
    }
}
