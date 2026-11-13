package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import java.net.URI
import java.net.URLEncoder

enum class SearchEngine(
    val displayName: String,
    val baseSearchUrl: String,
    @DrawableRes val iconRes: Int,
    val brandColor: Color,
) {
    DUCKDUCKGO(
        displayName = "DuckDuckGo",
        baseSearchUrl = "https://duckduckgo.com/?q=",
        iconRes = R.drawable.ic_brand_duckduckgo,
        brandColor = Color(0xFFDE5833),
    ),
    GOOGLE(
        displayName = "Google",
        baseSearchUrl = "https://www.google.com/search?q=",
        iconRes = R.drawable.ic_brand_google,
        brandColor = Color(0xFF4285F4),
    ),
    BING(
        displayName = "Bing",
        baseSearchUrl = "https://www.bing.com/search?q=",
        iconRes = R.drawable.ic_brand_bing,
        brandColor = Color(0xFF008373),
    ),
    BRAVE(
        displayName = "Brave Search",
        baseSearchUrl = "https://search.brave.com/search?q=",
        iconRes = R.drawable.ic_brand_brave,
        brandColor = Color(0xFFFB542B),
    ),
}

data class QuickLink(
    val title: String,
    val url: String,
    @DrawableRes val iconRes: Int,
    val accentColor: Color,
)

object DefaultSpeedDial {
    val links: List<QuickLink> = listOf(
        QuickLink("Wikipedia", "https://en.m.wikipedia.org", R.drawable.ic_brand_wikipedia, Color(0xFF64748B)),
        QuickLink("DuckDuckGo", "https://duckduckgo.com", R.drawable.ic_brand_duckduckgo, Color(0xFFDE5833)),
        QuickLink("GitHub", "https://github.com", R.drawable.ic_brand_github, Color(0xFF4F6D9A)),
        QuickLink("Android", "https://developer.android.com", R.drawable.ic_brand_android, Color(0xFF10B981)),
        QuickLink("MDN Docs", "https://developer.mozilla.org", R.drawable.ic_brand_mdn, Color(0xFF2563EB)),
        QuickLink("Hacker News", "https://news.ycombinator.com", R.drawable.ic_brand_ycombinator, Color(0xFFFF6600)),
        QuickLink("Maps", "https://www.openstreetmap.org", R.drawable.ic_brand_openstreetmap, Color(0xFF52A447)),
        QuickLink("Archive", "https://archive.org", R.drawable.ic_brand_archive, Color(0xFF8B5CF6)),
    )
}

object BrandIconLookup {
    @DrawableRes
    fun iconResForUrl(url: String): Int? {
        val host = UrlNormalizer.extractHost(url).lowercase()
        return when {
            "wikipedia.org" in host -> R.drawable.ic_brand_wikipedia
            "duckduckgo.com" in host -> R.drawable.ic_brand_duckduckgo
            "github.com" in host -> R.drawable.ic_brand_github
            "android.com" in host -> R.drawable.ic_brand_android
            "mozilla.org" in host -> R.drawable.ic_brand_mdn
            "ycombinator.com" in host -> R.drawable.ic_brand_ycombinator
            "openstreetmap.org" in host -> R.drawable.ic_brand_openstreetmap
            "archive.org" in host -> R.drawable.ic_brand_archive
            "google.com" in host -> R.drawable.ic_brand_google
            "bing.com" in host -> R.drawable.ic_brand_bing
            "brave.com" in host -> R.drawable.ic_brand_brave
            else -> null
        }
    }
}

data class BrowserTabState(
    val id: String,
    val url: String = "",
    val title: String = "",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isPrivate: Boolean = false,
    val isDesktopMode: Boolean = false,
    val errorMessage: String? = null,
) {
    val isStartPage: Boolean
        get() = url.isBlank()

    val isSecure: Boolean
        get() = url.startsWith("https://", ignoreCase = true)

    val displayTitle: String
        get() = when {
            title.isNotBlank() -> title
            url.isNotBlank() -> UrlNormalizer.extractHost(url)
            isPrivate -> "Private Tab"
            else -> "New Tab"
        }
}

object UrlNormalizer {
    private val schemeRegex = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")
    private val domainRegex = Regex("^([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/.*)?$")
    private val localhostOrIpRegex = Regex("^(localhost|\\d{1,3}(\\.\\d{1,3}){3})(:\\d+)?(/.*)?$", RegexOption.IGNORE_CASE)

    fun isLikelyUrlOrDomain(rawInput: String): Boolean {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return false
        return schemeRegex.matches(trimmed) ||
            localhostOrIpRegex.matches(trimmed) ||
            (!trimmed.contains(' ') && domainRegex.matches(trimmed))
    }

    fun resolveInput(rawInput: String, searchEngine: SearchEngine): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return ""

        return when {
            schemeRegex.matches(trimmed) -> trimmed
            localhostOrIpRegex.matches(trimmed) -> "http://$trimmed"
            !trimmed.contains(' ') && domainRegex.matches(trimmed) -> "https://$trimmed"
            else -> searchEngine.baseSearchUrl + URLEncoder.encode(trimmed, "UTF-8")
        }
    }

    fun extractHost(url: String): String {
        if (url.isBlank()) return ""
        return runCatching {
            val host = URI(url).host.orEmpty()
            host.removePrefix("www.").ifBlank { url }
        }.getOrElse { url }
    }
}
