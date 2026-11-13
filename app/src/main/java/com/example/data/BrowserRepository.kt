package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.BookmarkEntity
import com.example.data.local.BrowserDao
import com.example.data.local.HistoryEntity
import com.example.model.SearchEngine
import com.example.ui.theme.OrbitRadiusPreset
import com.example.ui.theme.OrbitThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.browserDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "orbit_browser_settings",
)

data class BrowserSettings(
    val searchEngine: SearchEngine = SearchEngine.DUCKDUCKGO,
    val javaScriptEnabled: Boolean = true,
    val desktopModeDefault: Boolean = false,
    val themePreset: OrbitThemePreset = OrbitThemePreset.RADIX_INDIGO,
    val radiusPreset: OrbitRadiusPreset = OrbitRadiusPreset.BALANCED,
)

class BrowserRepository(
    private val dao: BrowserDao,
    private val context: Context,
) {
    private object Keys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val JAVASCRIPT_ENABLED = booleanPreferencesKey("javascript_enabled")
        val DESKTOP_MODE_DEFAULT = booleanPreferencesKey("desktop_mode_default")
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val RADIUS_PRESET = stringPreferencesKey("radius_preset")
    }

    val bookmarksFlow: Flow<List<BookmarkEntity>> = dao.observeBookmarks()
    val historyFlow: Flow<List<HistoryEntity>> = dao.observeHistory()

    val settingsFlow: Flow<BrowserSettings> = context.browserDataStore.data.map { prefs ->
        val engineName = prefs[Keys.SEARCH_ENGINE]
        val engine = SearchEngine.entries.firstOrNull { it.name == engineName }
            ?: SearchEngine.DUCKDUCKGO
        val themeName = prefs[Keys.THEME_PRESET]
        val themePreset = OrbitThemePreset.entries.firstOrNull { it.name == themeName }
            ?: OrbitThemePreset.RADIX_INDIGO
        val radiusName = prefs[Keys.RADIUS_PRESET]
        val radiusPreset = OrbitRadiusPreset.entries.firstOrNull { it.name == radiusName }
            ?: OrbitRadiusPreset.BALANCED
        BrowserSettings(
            searchEngine = engine,
            javaScriptEnabled = prefs[Keys.JAVASCRIPT_ENABLED] ?: true,
            desktopModeDefault = prefs[Keys.DESKTOP_MODE_DEFAULT] ?: false,
            themePreset = themePreset,
            radiusPreset = radiusPreset,
        )
    }

    suspend fun toggleBookmark(url: String, title: String) {
        dao.insertBookmark(
            BookmarkEntity(
                url = url,
                title = title.ifBlank { url },
            ),
        )
    }

    suspend fun removeBookmark(url: String) {
        dao.deleteBookmarkByUrl(url)
    }

    suspend fun recordVisit(url: String, title: String) {
        if (url.isBlank() || url.startsWith("about:")) return
        dao.insertHistory(
            HistoryEntity(
                url = url,
                title = title.ifBlank { url },
            ),
        )
    }

    suspend fun deleteHistoryItem(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun setSearchEngine(engine: SearchEngine) {
        context.browserDataStore.edit { it[Keys.SEARCH_ENGINE] = engine.name }
    }

    suspend fun setJavaScriptEnabled(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.JAVASCRIPT_ENABLED] = enabled }
    }

    suspend fun setDesktopModeDefault(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.DESKTOP_MODE_DEFAULT] = enabled }
    }

    suspend fun setThemePreset(preset: OrbitThemePreset) {
        context.browserDataStore.edit { it[Keys.THEME_PRESET] = preset.name }
    }

    suspend fun setRadiusPreset(radiusPreset: OrbitRadiusPreset) {
        context.browserDataStore.edit { it[Keys.RADIUS_PRESET] = radiusPreset.name }
    }
}
