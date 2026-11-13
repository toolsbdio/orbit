package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.BrowserModalSheets
import com.example.ui.components.BrowserStartPage
import com.example.ui.components.BrowserWebViewHost
import com.example.ui.components.FindInPageBar
import com.example.ui.components.OmniboxBar
import com.example.ui.components.OmniboxSuggestionsDropdown
import com.example.ui.theme.LocalOrbitTokens
import com.example.ui.theme.OrbitTheme
import com.example.ui.theme.PrivateViolet
import com.example.ui.theme.WarningAmber

@Composable
fun OrbitBrowserApp(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OrbitTheme(
        preset = state.settings.themePreset,
        radiusPreset = state.settings.radiusPreset,
    ) {
        OrbitBrowserContent(
            viewModel = viewModel,
            state = state,
            modifier = modifier,
        )
    }
}

@Composable
private fun OrbitBrowserContent(
    viewModel: BrowserViewModel,
    state: BrowserUiState,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalOrbitTokens.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearStatusMessage()
    }

    val activeTab = state.activeTab

    BackHandler(
        enabled = state.isFindInPageVisible ||
            state.isOmniboxFocused ||
            activeTab.canGoBack ||
            !activeTab.isStartPage,
    ) {
        when {
            state.isFindInPageVisible -> viewModel.showFindInPage(false)
            state.isOmniboxFocused -> focusManager.clearFocus()
            activeTab.canGoBack || !activeTab.isStartPage -> viewModel.goBack()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            containerColor = tokens.base200,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(tokens.controlRadius),
                        containerColor = tokens.baseContent,
                        contentColor = tokens.base100,
                    )
                }
            },
            topBar = {
                Surface(
                    color = tokens.base100,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (isWide) {
                                BrowserNavControlsRow(
                                    canGoBack = activeTab.canGoBack || !activeTab.isStartPage,
                                    canGoForward = activeTab.canGoForward,
                                    onBack = viewModel::goBack,
                                    onForward = viewModel::goForward,
                                    onHome = viewModel::goHome,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (isWide) Modifier.widthIn(max = 700.dp) else Modifier,
                                    ),
                            ) {
                                OmniboxBar(
                                    tab = activeTab,
                                    omniboxText = state.omniboxText,
                                    isFocused = state.isOmniboxFocused,
                                    onTextChange = viewModel::onOmniboxTextChange,
                                    onFocusChange = viewModel::onOmniboxFocusChange,
                                    onSubmit = { viewModel.submitOmnibox() },
                                    onReloadOrStop = viewModel::reloadOrStop,
                                )
                            }

                            if (!activeTab.isStartPage) {
                                IconButton(
                                    onClick = viewModel::toggleBookmarkForActiveTab,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("top_bookmark_button"),
                                ) {
                                    Icon(
                                        imageVector = if (state.isCurrentPageBookmarked) {
                                            Icons.Filled.Star
                                        } else {
                                            Icons.Filled.StarOutline
                                        },
                                        contentDescription = if (state.isCurrentPageBookmarked) {
                                            stringResource(R.string.bookmark_remove_cd)
                                        } else {
                                            stringResource(R.string.bookmark_add_cd)
                                        },
                                        tint = if (state.isCurrentPageBookmarked) {
                                            WarningAmber
                                        } else {
                                            tokens.mutedContent
                                        },
                                    )
                                }
                            }

                            if (isWide) {
                                TabsCountButton(
                                    tabCount = state.tabs.size,
                                    isPrivate = activeTab.isPrivate,
                                    onClick = { viewModel.openSheet(BrowserSheet.TABS) },
                                )
                                IconButton(
                                    onClick = { viewModel.openSheet(BrowserSheet.MENU) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("more_menu_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = stringResource(R.string.more_menu_cd),
                                        tint = tokens.baseContent,
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = state.isFindInPageVisible) {
                            FindInPageBar(
                                query = state.findQuery,
                                activeMatch = state.findActiveMatch,
                                totalMatches = state.findTotalMatches,
                                onQueryChange = viewModel::updateFindQuery,
                                onFindPrev = { viewModel.findNext(forward = false) },
                                onFindNext = { viewModel.findNext(forward = true) },
                                onClose = { viewModel.showFindInPage(false) },
                            )
                        }

                        HorizontalDivider(color = tokens.borderSubtle)
                    }
                }
            },
            bottomBar = {
                if (!isWide) {
                    // daisyUI Floating Dock Bar with Radix hairline framing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(tokens.cardRadius + 4.dp),
                            color = tokens.base100,
                            tonalElevation = 0.dp,
                            shadowElevation = 6.dp,
                            border = BorderStroke(
                                1.dp,
                                if (activeTab.isPrivate) {
                                    PrivateViolet.copy(alpha = 0.55f)
                                } else {
                                    tokens.borderStrong
                                },
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BrowserNavControlsRow(
                                    canGoBack = activeTab.canGoBack || !activeTab.isStartPage,
                                    canGoForward = activeTab.canGoForward,
                                    onBack = viewModel::goBack,
                                    onForward = viewModel::goForward,
                                    onHome = viewModel::goHome,
                                )
                                TabsCountButton(
                                    tabCount = state.tabs.size,
                                    isPrivate = activeTab.isPrivate,
                                    onClick = { viewModel.openSheet(BrowserSheet.TABS) },
                                )
                                IconButton(
                                    onClick = { viewModel.openSheet(BrowserSheet.MENU) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("more_menu_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = stringResource(R.string.more_menu_cd),
                                        tint = tokens.baseContent,
                                    )
                                }
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (activeTab.isStartPage) {
                    key(activeTab.id) {
                        BrowserStartPage(
                            isPrivate = activeTab.isPrivate,
                            bookmarks = state.bookmarks,
                            recentHistory = state.history,
                            onOpenUrl = viewModel::navigateActiveTab,
                        )
                    }
                } else {
                    BrowserWebViewHost(
                        activeTab = activeTab,
                        settings = state.settings,
                        commands = viewModel.commands,
                        onPageStarted = viewModel::onPageStarted,
                        onPageFinished = viewModel::onPageFinished,
                        onProgressChanged = viewModel::onProgressChanged,
                        onTitleChanged = viewModel::onTitleChanged,
                        onNavigationStateChanged = viewModel::onNavigationStateChanged,
                        onPageError = viewModel::onPageError,
                        onFindMatchesUpdated = viewModel::onFindMatchesUpdated,
                        onReloadClick = viewModel::reloadOrStop,
                    )
                }

                // Live Autocomplete Suggestions Overlay
                OmniboxSuggestionsDropdown(
                    suggestions = state.suggestions,
                    searchEngine = state.settings.searchEngine,
                    onSelectSuggestion = { url ->
                        viewModel.navigateActiveTab(url)
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }

    BrowserModalSheets(
        state = state,
        onDismiss = { viewModel.openSheet(null) },
        onOpenSheet = { sheet -> viewModel.openSheet(sheet) },
        onNewTab = { isPrivate -> viewModel.createNewTab(isPrivate = isPrivate) },
        onSwitchTab = viewModel::switchToTab,
        onCloseTab = viewModel::closeTab,
        onToggleBookmark = viewModel::toggleBookmarkForActiveTab,
        onOpenUrl = viewModel::navigateActiveTab,
        onRemoveBookmark = viewModel::removeBookmark,
        onDeleteHistoryItem = viewModel::deleteHistoryItem,
        onClearHistory = viewModel::clearHistory,
        onOpenFindInPage = { viewModel.showFindInPage(true) },
        onShareCurrentPage = {
            if (activeTab.url.isNotBlank()) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, activeTab.displayTitle)
                    putExtra(Intent.EXTRA_TEXT, activeTab.url)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share link"))
            }
        },
        onToggleDesktopMode = viewModel::toggleDesktopModeForActiveTab,
        onSelectSearchEngine = viewModel::setSearchEngine,
        onToggleJavaScript = viewModel::setJavaScriptEnabled,
        onToggleDesktopDefault = viewModel::setDesktopModeDefault,
        onSelectThemePreset = viewModel::setThemePreset,
        onSelectRadiusPreset = viewModel::setRadiusPreset,
    )
}

@Composable
private fun BrowserNavControlsRow(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
) {
    val tokens = LocalOrbitTokens.current
    IconButton(
        onClick = onBack,
        enabled = canGoBack,
        modifier = Modifier
            .size(48.dp)
            .testTag("nav_back_button"),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.nav_back_cd),
            tint = if (canGoBack) tokens.baseContent else tokens.mutedContent.copy(alpha = 0.4f),
        )
    }
    IconButton(
        onClick = onForward,
        enabled = canGoForward,
        modifier = Modifier
            .size(48.dp)
            .testTag("nav_forward_button"),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(R.string.nav_forward_cd),
            tint = if (canGoForward) tokens.baseContent else tokens.mutedContent.copy(alpha = 0.4f),
        )
    }
    IconButton(
        onClick = onHome,
        modifier = Modifier
            .size(48.dp)
            .testTag("nav_home_button"),
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = stringResource(R.string.nav_home_cd),
            tint = tokens.baseContent,
        )
    }
}

@Composable
private fun TabsCountButton(
    tabCount: Int,
    isPrivate: Boolean,
    onClick: () -> Unit,
) {
    val tokens = LocalOrbitTokens.current
    val borderColor = if (isPrivate) PrivateViolet else tokens.baseContent
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .testTag("tabs_switcher_button"),
    ) {
        Surface(
            shape = RoundedCornerShape((tokens.controlRadius * 0.55f).coerceAtLeast(6.dp)),
            border = BorderStroke(1.8.dp, borderColor),
            color = if (isPrivate) PrivateViolet.copy(alpha = 0.16f) else tokens.base200,
            modifier = Modifier.size(25.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = tabCount.coerceAtMost(99).toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = borderColor,
                )
            }
        }
    }
}
