package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BrowserRepository
import com.example.data.BrowserSettings
import com.example.data.SearchSuggestionService
import com.example.data.local.BookmarkEntity
import com.example.data.local.HistoryEntity
import com.example.model.BrowserTabState
import com.example.model.SearchEngine
import com.example.model.UrlNormalizer
import com.example.ui.theme.OrbitRadiusPreset
import com.example.ui.theme.OrbitThemePreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class BrowserSheet {
    MENU,
    TABS,
    BOOKMARKS,
    HISTORY,
    SETTINGS,
}

enum class SuggestionKind {
    BOOKMARK,
    HISTORY,
    SEARCH_ENGINE,
}

data class OmniboxSuggestion(
    val title: String,
    val url: String,
    val kind: SuggestionKind,
    val subtitle: String = url,
)

sealed interface BrowserWebCommand {
    val tabId: String

    data class LoadUrl(override val tabId: String, val url: String) : BrowserWebCommand
    data class Reload(override val tabId: String) : BrowserWebCommand
    data class StopLoading(override val tabId: String) : BrowserWebCommand
    data class GoBack(override val tabId: String) : BrowserWebCommand
    data class GoForward(override val tabId: String) : BrowserWebCommand
    data class FindAll(override val tabId: String, val query: String) : BrowserWebCommand
    data class FindNext(override val tabId: String, val forward: Boolean) : BrowserWebCommand
    data class ClearFind(override val tabId: String) : BrowserWebCommand
    data class DestroyTab(override val tabId: String) : BrowserWebCommand
}

data class BrowserUiState(
    val tabs: List<BrowserTabState> = listOf(BrowserTabState(id = UUID.randomUUID().toString())),
    val activeTabIndex: Int = 0,
    val omniboxText: String = "",
    val isOmniboxFocused: Boolean = false,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val history: List<HistoryEntity> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),
    val settings: BrowserSettings = BrowserSettings(),
    val activeSheet: BrowserSheet? = null,
    val isFindInPageVisible: Boolean = false,
    val findQuery: String = "",
    val findActiveMatch: Int = 0,
    val findTotalMatches: Int = 0,
    val statusMessage: String? = null,
) {
    val activeTab: BrowserTabState
        get() = tabs.getOrElse(activeTabIndex) { tabs.first() }

    val isCurrentPageBookmarked: Boolean
        get() = activeTab.url.isNotBlank() && bookmarks.any { it.url == activeTab.url }

    val suggestions: List<OmniboxSuggestion>
        get() {
            if (!isOmniboxFocused || omniboxText.isBlank()) return emptyList()
            val rawTrimmed = omniboxText.trim()
            val query = rawTrimmed.lowercase()
            val engine = settings.searchEngine

            val bookmarkMatches = bookmarks
                .filter { it.url.lowercase().contains(query) || it.title.lowercase().contains(query) }
                .take(3)
                .map {
                    OmniboxSuggestion(
                        title = it.title,
                        url = it.url,
                        kind = SuggestionKind.BOOKMARK,
                        subtitle = it.url,
                    )
                }
            val historyMatches = history
                .filter { entry ->
                    (entry.url.lowercase().contains(query) || entry.title.lowercase().contains(query)) &&
                        bookmarkMatches.none { it.url == entry.url }
                }
                .distinctBy { it.url }
                .take(3)
                .map {
                    OmniboxSuggestion(
                        title = it.title,
                        url = it.url,
                        kind = SuggestionKind.HISTORY,
                        subtitle = it.url,
                    )
                }

            if (UrlNormalizer.isLikelyUrlOrDomain(rawTrimmed)) {
                return (bookmarkMatches + historyMatches).take(6)
            }

            val searchPhrases = buildList {
                add(rawTrimmed)
                searchSuggestions.forEach { suggestion ->
                    if (suggestion.isNotBlank() && none { it.equals(suggestion, ignoreCase = true) }) {
                        add(suggestion)
                    }
                }
            }.take(5)

            val searchItems = searchPhrases.map { phrase ->
                OmniboxSuggestion(
                    title = phrase,
                    url = UrlNormalizer.resolveInput(phrase, engine),
                    kind = SuggestionKind.SEARCH_ENGINE,
                    subtitle = "Search with ${engine.displayName}",
                )
            }

            return (bookmarkMatches + historyMatches + searchItems).take(7)
        }
}

class BrowserViewModel(
    private val repository: BrowserRepository,
    private val suggestionService: SearchSuggestionService = SearchSuggestionService(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _commands = MutableSharedFlow<BrowserWebCommand>(extraBufferCapacity = 32)
    val commands: SharedFlow<BrowserWebCommand> = _commands.asSharedFlow()

    private var suggestionJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                repository.bookmarksFlow,
                repository.historyFlow,
                repository.settingsFlow,
            ) { bookmarks, history, settings ->
                Triple(bookmarks, history, settings)
            }.collect { (bookmarks, history, settings) ->
                _uiState.update { state ->
                    state.copy(
                        bookmarks = bookmarks,
                        history = history,
                        settings = settings,
                    )
                }
            }
        }
    }

    fun onOmniboxTextChange(text: String) {
        _uiState.update { it.copy(omniboxText = text) }
        refreshSearchSuggestions(text)
    }

    fun onOmniboxFocusChange(focused: Boolean) {
        _uiState.update { state ->
            val updatedText = if (!focused) state.activeTab.url else state.omniboxText
            state.copy(
                isOmniboxFocused = focused,
                omniboxText = updatedText,
                searchSuggestions = if (!focused) emptyList() else state.searchSuggestions,
            )
        }
        if (focused) {
            refreshSearchSuggestions(_uiState.value.omniboxText)
        } else {
            suggestionJob?.cancel()
        }
    }

    private fun refreshSearchSuggestions(rawQuery: String) {
        suggestionJob?.cancel()
        val trimmed = rawQuery.trim()
        val currentState = _uiState.value
        if (!currentState.isOmniboxFocused || trimmed.length < 2 || UrlNormalizer.isLikelyUrlOrDomain(trimmed)) {
            _uiState.update { it.copy(searchSuggestions = emptyList()) }
            return
        }
        val engine = currentState.settings.searchEngine
        suggestionJob = viewModelScope.launch {
            delay(180)
            val fetched = suggestionService.fetchSuggestions(trimmed, engine)
            val latest = _uiState.value
            if (latest.isOmniboxFocused && latest.omniboxText.trim() == trimmed) {
                _uiState.update { it.copy(searchSuggestions = fetched) }
            }
        }
    }

    fun submitOmnibox(rawInput: String = _uiState.value.omniboxText) {
        val resolved = UrlNormalizer.resolveInput(rawInput, _uiState.value.settings.searchEngine)
        if (resolved.isBlank()) return
        navigateActiveTab(resolved)
    }

    fun navigateActiveTab(url: String) {
        val normalized = UrlNormalizer.resolveInput(url, _uiState.value.settings.searchEngine)
        if (normalized.isBlank()) return
        suggestionJob?.cancel()
        val tab = _uiState.value.activeTab
        updateTab(tab.id) {
            it.copy(
                url = normalized,
                isLoading = true,
                progress = 10,
                errorMessage = null,
            )
        }
        _uiState.update {
            it.copy(
                omniboxText = normalized,
                isOmniboxFocused = false,
                searchSuggestions = emptyList(),
                activeSheet = null,
                isFindInPageVisible = false,
            )
        }
        _commands.tryEmit(BrowserWebCommand.LoadUrl(tab.id, normalized))
    }

    fun goHome() {
        val tab = _uiState.value.activeTab
        _commands.tryEmit(BrowserWebCommand.StopLoading(tab.id))
        updateTab(tab.id) {
            it.copy(
                url = "",
                title = "",
                progress = 0,
                isLoading = false,
                errorMessage = null,
            )
        }
        _uiState.update {
            it.copy(
                omniboxText = "",
                isOmniboxFocused = false,
                isFindInPageVisible = false,
            )
        }
    }

    fun reloadOrStop() {
        val tab = _uiState.value.activeTab
        if (tab.isStartPage) return
        if (tab.isLoading) {
            _commands.tryEmit(BrowserWebCommand.StopLoading(tab.id))
            updateTab(tab.id) { it.copy(isLoading = false, progress = 0) }
        } else {
            updateTab(tab.id) { it.copy(isLoading = true, progress = 10, errorMessage = null) }
            _commands.tryEmit(BrowserWebCommand.Reload(tab.id))
        }
    }

    fun goBack() {
        val tab = _uiState.value.activeTab
        if (tab.canGoBack) {
            _commands.tryEmit(BrowserWebCommand.GoBack(tab.id))
        } else if (!tab.isStartPage) {
            goHome()
        }
    }

    fun goForward() {
        val tab = _uiState.value.activeTab
        if (tab.canGoForward) {
            _commands.tryEmit(BrowserWebCommand.GoForward(tab.id))
        }
    }

    // WebView callbacks
    fun onPageStarted(tabId: String, url: String) {
        updateTab(tabId) {
            it.copy(
                url = url,
                isLoading = true,
                progress = maxOf(it.progress, 15),
                errorMessage = null,
            )
        }
        if (tabId == _uiState.value.activeTab.id && !_uiState.value.isOmniboxFocused) {
            _uiState.update { it.copy(omniboxText = url) }
        }
    }

    fun onPageFinished(tabId: String, url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) {
        val tab = _uiState.value.tabs.firstOrNull { it.id == tabId } ?: return
        val resolvedTitle = title.ifBlank { UrlNormalizer.extractHost(url) }
        updateTab(tabId) {
            it.copy(
                url = url,
                title = resolvedTitle,
                isLoading = false,
                progress = 100,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
            )
        }
        if (tabId == _uiState.value.activeTab.id && !_uiState.value.isOmniboxFocused) {
            _uiState.update { it.copy(omniboxText = url) }
        }
        if (!tab.isPrivate && url.isNotBlank()) {
            viewModelScope.launch {
                repository.recordVisit(url = url, title = resolvedTitle)
            }
        }
    }

    fun onProgressChanged(tabId: String, progress: Int) {
        updateTab(tabId) {
            it.copy(
                progress = progress,
                isLoading = progress in 1..99,
            )
        }
    }

    fun onTitleChanged(tabId: String, title: String) {
        if (title.isBlank()) return
        updateTab(tabId) { it.copy(title = title) }
    }

    fun onNavigationStateChanged(tabId: String, canGoBack: Boolean, canGoForward: Boolean) {
        updateTab(tabId) { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    fun onPageError(tabId: String, description: String) {
        updateTab(tabId) {
            it.copy(
                isLoading = false,
                progress = 0,
                errorMessage = description.ifBlank { "Unable to load this page" },
            )
        }
    }

    // Tabs management
    fun createNewTab(isPrivate: Boolean = false, initialUrl: String = "") {
        val settings = _uiState.value.settings
        val newTab = BrowserTabState(
            id = UUID.randomUUID().toString(),
            url = initialUrl,
            isPrivate = isPrivate,
            isDesktopMode = settings.desktopModeDefault,
        )
        _uiState.update { state ->
            val updated = state.tabs + newTab
            state.copy(
                tabs = updated,
                activeTabIndex = updated.lastIndex,
                omniboxText = initialUrl,
                isOmniboxFocused = false,
                activeSheet = null,
                isFindInPageVisible = false,
            )
        }
        if (initialUrl.isNotBlank()) {
            _commands.tryEmit(BrowserWebCommand.LoadUrl(newTab.id, initialUrl))
        }
    }

    fun switchToTab(index: Int) {
        val state = _uiState.value
        if (index !in state.tabs.indices) return
        val target = state.tabs[index]
        _uiState.update {
            it.copy(
                activeTabIndex = index,
                omniboxText = target.url,
                isOmniboxFocused = false,
                activeSheet = null,
                isFindInPageVisible = false,
            )
        }
    }

    fun closeTab(index: Int) {
        val state = _uiState.value
        if (index !in state.tabs.indices) return
        val removed = state.tabs[index]
        _commands.tryEmit(BrowserWebCommand.DestroyTab(removed.id))

        if (state.tabs.size == 1) {
            val replacement = BrowserTabState(
                id = UUID.randomUUID().toString(),
                isDesktopMode = state.settings.desktopModeDefault,
            )
            _uiState.update {
                it.copy(
                    tabs = listOf(replacement),
                    activeTabIndex = 0,
                    omniboxText = "",
                    isOmniboxFocused = false,
                    activeSheet = null,
                    isFindInPageVisible = false,
                )
            }
            return
        }

        val remaining = state.tabs.toMutableList().apply { removeAt(index) }
        val newActiveIndex = when {
            index < state.activeTabIndex -> state.activeTabIndex - 1
            index == state.activeTabIndex -> index.coerceAtMost(remaining.lastIndex)
            else -> state.activeTabIndex
        }
        val active = remaining[newActiveIndex]
        _uiState.update {
            it.copy(
                tabs = remaining,
                activeTabIndex = newActiveIndex,
                omniboxText = active.url,
                isFindInPageVisible = false,
            )
        }
    }

    // Bookmarks & History
    fun toggleBookmarkForActiveTab() {
        val tab = _uiState.value.activeTab
        if (tab.url.isBlank()) return
        val alreadySaved = _uiState.value.bookmarks.any { it.url == tab.url }
        viewModelScope.launch {
            if (alreadySaved) {
                repository.removeBookmark(tab.url)
                showMessage("Bookmark removed")
            } else {
                repository.toggleBookmark(tab.url, tab.displayTitle)
                showMessage("Saved to Bookmarks")
            }
        }
    }

    fun removeBookmark(url: String) {
        viewModelScope.launch { repository.removeBookmark(url) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteHistoryItem(id) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showMessage("History cleared")
        }
    }

    // Desktop mode & Find in Page & Settings
    fun toggleDesktopModeForActiveTab() {
        val tab = _uiState.value.activeTab
        val next = !tab.isDesktopMode
        updateTab(tab.id) { it.copy(isDesktopMode = next) }
        _uiState.update { it.copy(activeSheet = null) }
        if (!tab.isStartPage) {
            _commands.tryEmit(BrowserWebCommand.Reload(tab.id))
        }
        showMessage(if (next) "Desktop site enabled" else "Mobile site enabled")
    }

    fun openSheet(sheet: BrowserSheet?) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun showFindInPage(show: Boolean) {
        val tabId = _uiState.value.activeTab.id
        if (!show) {
            _commands.tryEmit(BrowserWebCommand.ClearFind(tabId))
        }
        _uiState.update {
            it.copy(
                isFindInPageVisible = show,
                findQuery = if (show) it.findQuery else "",
                findActiveMatch = 0,
                findTotalMatches = 0,
                activeSheet = null,
            )
        }
    }

    fun updateFindQuery(query: String) {
        val tabId = _uiState.value.activeTab.id
        _uiState.update { it.copy(findQuery = query) }
        if (query.isBlank()) {
            _commands.tryEmit(BrowserWebCommand.ClearFind(tabId))
            _uiState.update { it.copy(findActiveMatch = 0, findTotalMatches = 0) }
        } else {
            _commands.tryEmit(BrowserWebCommand.FindAll(tabId, query))
        }
    }

    fun findNext(forward: Boolean) {
        val tabId = _uiState.value.activeTab.id
        _commands.tryEmit(BrowserWebCommand.FindNext(tabId, forward))
    }

    fun onFindMatchesUpdated(activeOrdinal: Int, total: Int) {
        _uiState.update {
            it.copy(
                findActiveMatch = if (total > 0) activeOrdinal + 1 else 0,
                findTotalMatches = total,
            )
        }
    }

    fun setSearchEngine(engine: SearchEngine) {
        viewModelScope.launch { repository.setSearchEngine(engine) }
    }

    fun setJavaScriptEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setJavaScriptEnabled(enabled) }
    }

    fun setDesktopModeDefault(enabled: Boolean) {
        viewModelScope.launch { repository.setDesktopModeDefault(enabled) }
    }

    fun setThemePreset(preset: OrbitThemePreset) {
        viewModelScope.launch { repository.setThemePreset(preset) }
    }

    fun setRadiusPreset(radiusPreset: OrbitRadiusPreset) {
        viewModelScope.launch { repository.setRadiusPreset(radiusPreset) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun showMessage(msg: String) {
        _uiState.update { it.copy(statusMessage = msg) }
    }

    private fun updateTab(tabId: String, transform: (BrowserTabState) -> BrowserTabState) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) transform(tab) else tab
                },
            )
        }
    }

    class Factory(private val repository: BrowserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BrowserViewModel(repository) as T
        }
    }
}
