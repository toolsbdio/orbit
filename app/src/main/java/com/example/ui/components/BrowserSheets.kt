package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BrowserSettings
import com.example.data.local.BookmarkEntity
import com.example.data.local.HistoryEntity
import com.example.model.BrandIconLookup
import com.example.model.BrowserTabState
import com.example.model.SearchEngine
import com.example.model.UrlNormalizer
import com.example.ui.BrowserSheet
import com.example.ui.BrowserUiState
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalOrbitTokens
import com.example.ui.theme.OrbitRadiusPreset
import com.example.ui.theme.OrbitThemePreset
import com.example.ui.theme.PrivateViolet
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserModalSheets(
    state: BrowserUiState,
    onDismiss: () -> Unit,
    onOpenSheet: (BrowserSheet) -> Unit,
    onNewTab: (isPrivate: Boolean) -> Unit,
    onSwitchTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onShareCurrentPage: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onSelectSearchEngine: (SearchEngine) -> Unit,
    onToggleJavaScript: (Boolean) -> Unit,
    onToggleDesktopDefault: (Boolean) -> Unit,
    onSelectThemePreset: (OrbitThemePreset) -> Unit,
    onSelectRadiusPreset: (OrbitRadiusPreset) -> Unit,
) {
    val activeSheet = state.activeSheet ?: return
    val tokens = LocalOrbitTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = tokens.cardRadius + 10.dp,
            topEnd = tokens.cardRadius + 10.dp,
        ),
        containerColor = tokens.base100,
        contentColor = tokens.baseContent,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = tokens.borderStrong)
        },
    ) {
        when (activeSheet) {
            BrowserSheet.MENU -> BrowserMenuContent(
                state = state,
                onNewTab = { onNewTab(false) },
                onNewPrivateTab = { onNewTab(true) },
                onToggleBookmark = {
                    onToggleBookmark()
                    onDismiss()
                },
                onOpenBookmarks = { onOpenSheet(BrowserSheet.BOOKMARKS) },
                onOpenHistory = { onOpenSheet(BrowserSheet.HISTORY) },
                onFindInPage = onOpenFindInPage,
                onShare = {
                    onShareCurrentPage()
                    onDismiss()
                },
                onToggleDesktopMode = onToggleDesktopMode,
                onOpenSettings = { onOpenSheet(BrowserSheet.SETTINGS) },
            )

            BrowserSheet.TABS -> TabsSwitcherContent(
                tabs = state.tabs,
                activeTabIndex = state.activeTabIndex,
                onNewTab = { onNewTab(false) },
                onNewPrivateTab = { onNewTab(true) },
                onSwitchTab = onSwitchTab,
                onCloseTab = onCloseTab,
            )

            BrowserSheet.BOOKMARKS -> BookmarksContent(
                bookmarks = state.bookmarks,
                onOpenUrl = onOpenUrl,
                onRemoveBookmark = onRemoveBookmark,
            )

            BrowserSheet.HISTORY -> HistoryContent(
                history = state.history,
                onOpenUrl = onOpenUrl,
                onDeleteEntry = onDeleteHistoryItem,
                onClearAll = onClearHistory,
            )

            BrowserSheet.SETTINGS -> SettingsContent(
                settings = state.settings,
                onSelectSearchEngine = onSelectSearchEngine,
                onToggleJavaScript = onToggleJavaScript,
                onToggleDesktopDefault = onToggleDesktopDefault,
                onSelectThemePreset = onSelectThemePreset,
                onSelectRadiusPreset = onSelectRadiusPreset,
                onClearHistory = onClearHistory,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BrowserMenuContent(
    state: BrowserUiState,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onFindInPage: () -> Unit,
    onShare: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val tokens = LocalOrbitTokens.current
    val tab = state.activeTab
    val isWebPage = !tab.isStartPage

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .testTag("browser_menu_sheet"),
    ) {
        RadixSurfaceCard(
            useElevatedTint = true,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape((tokens.controlRadius * 0.75f).coerceAtLeast(6.dp)))
                        .background(
                            if (tab.isPrivate) PrivateViolet.copy(alpha = 0.16f) else tokens.accentSoft,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (tab.isPrivate) Icons.Filled.VisibilityOff else Icons.Filled.Public,
                        contentDescription = null,
                        tint = if (tab.isPrivate) PrivateViolet else tokens.accentSolid,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tab.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.baseContent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (tab.url.isNotBlank()) tab.url else "Orbit Start Page",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = tokens.mutedContent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (tab.isPrivate) {
                    RadixBadge(
                        text = "Private",
                        variant = RadixBadgeVariant.SOFT,
                        accentColor = PrivateViolet,
                        softBgColor = PrivateViolet.copy(alpha = 0.18f),
                        softTextColor = PrivateViolet,
                    )
                } else if (tab.isDesktopMode) {
                    RadixBadge(
                        text = "Desktop",
                        variant = RadixBadgeVariant.SOFT,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 3,
        ) {
            MenuActionTile(
                icon = Icons.Filled.Add,
                label = stringResource(R.string.action_new_tab),
                onClick = onNewTab,
                testTag = "menu_new_tab",
                modifier = Modifier.weight(1f),
            )
            MenuActionTile(
                icon = Icons.Filled.VisibilityOff,
                label = stringResource(R.string.action_new_private_tab),
                accentTint = PrivateViolet,
                onClick = onNewPrivateTab,
                testTag = "menu_private_tab",
                modifier = Modifier.weight(1f),
            )
            MenuActionTile(
                icon = if (state.isCurrentPageBookmarked) Icons.Filled.Star else Icons.Filled.StarOutline,
                label = if (state.isCurrentPageBookmarked) "Bookmarked" else "Bookmark",
                enabled = isWebPage,
                accentTint = if (state.isCurrentPageBookmarked) WarningAmber else null,
                onClick = onToggleBookmark,
                testTag = "menu_bookmark_toggle",
                modifier = Modifier.weight(1f),
            )
            MenuActionTile(
                icon = Icons.Filled.Bookmarks,
                label = stringResource(R.string.action_bookmarks),
                onClick = onOpenBookmarks,
                testTag = "menu_bookmarks",
                modifier = Modifier.weight(1f),
            )
            MenuActionTile(
                icon = Icons.Filled.History,
                label = stringResource(R.string.action_history),
                onClick = onOpenHistory,
                testTag = "menu_history",
                modifier = Modifier.weight(1f),
            )
            MenuActionTile(
                icon = Icons.Filled.Search,
                label = stringResource(R.string.action_find_in_page),
                enabled = isWebPage,
                onClick = onFindInPage,
                testTag = "menu_find_in_page",
                modifier = Modifier.weight(1f),
            )
            MenuActionTile(
                icon = Icons.Filled.DesktopWindows,
                label = stringResource(R.string.action_desktop_site),
                isActive = tab.isDesktopMode,
                onClick = onToggleDesktopMode,
                testTag = "menu_desktop_site",
                modifier = Modifier.weight(1f),
            )
            MenuActionTile(
                icon = Icons.Filled.Share,
                label = stringResource(R.string.action_share),
                enabled = isWebPage,
                onClick = onShare,
                testTag = "menu_share",
                modifier = Modifier.weight(1f),
            )
            MenuActionTile(
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.action_settings),
                onClick = onOpenSettings,
                testTag = "menu_settings",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MenuActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isActive: Boolean = false,
    accentTint: Color? = null,
) {
    val tokens = LocalOrbitTokens.current
    val bgColor = when {
        !enabled -> tokens.base200.copy(alpha = 0.45f)
        isActive -> tokens.accentSoft
        else -> tokens.base200
    }
    val borderColor = when {
        !enabled -> tokens.borderSubtle.copy(alpha = 0.4f)
        isActive -> tokens.accentBorder
        else -> tokens.borderSubtle
    }
    val fgColor = when {
        !enabled -> tokens.mutedContent.copy(alpha = 0.4f)
        accentTint != null -> accentTint
        isActive -> tokens.accentSoftText
        else -> tokens.baseContent
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(tokens.controlRadius),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(80.dp)
            .testTag(testTag),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) tokens.baseContent else fgColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TabsSwitcherContent(
    tabs: List<BrowserTabState>,
    activeTabIndex: Int,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onSwitchTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
) {
    val tokens = LocalOrbitTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("tabs_switcher_sheet"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Tabs",
                    style = MaterialTheme.typography.titleLarge,
                    color = tokens.baseContent,
                )
                RadixBadge(
                    text = "${tabs.size}",
                    variant = RadixBadgeVariant.SOFT,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNewPrivateTab,
                    shape = RoundedCornerShape(tokens.controlRadius),
                    border = BorderStroke(1.dp, PrivateViolet.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrivateViolet),
                    modifier = Modifier.testTag("tabs_new_private_button"),
                ) {
                    Icon(Icons.Filled.VisibilityOff, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Private")
                }
                Button(
                    onClick = onNewTab,
                    shape = RoundedCornerShape(tokens.controlRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.accentSolid,
                        contentColor = if (tokens.isDark) Color(0xFF0B0E17) else Color.White,
                    ),
                    modifier = Modifier.testTag("tabs_new_button"),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Tab")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(tabs, key = { _, t -> t.id }) { idx, tab ->
                val isSelected = idx == activeTabIndex
                Surface(
                    onClick = { onSwitchTab(idx) },
                    shape = RoundedCornerShape(tokens.cardRadius),
                    color = when {
                        tab.isPrivate && isSelected -> PrivateViolet.copy(alpha = 0.16f)
                        isSelected -> tokens.accentSoft
                        else -> tokens.base200
                    },
                    border = BorderStroke(
                        width = if (isSelected) 1.6.dp else 1.dp,
                        color = when {
                            tab.isPrivate -> PrivateViolet
                            isSelected -> tokens.accentSolid
                            else -> tokens.borderSubtle
                        },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tab_item_$idx"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape((tokens.controlRadius * 0.75f).coerceAtLeast(6.dp)))
                                .background(
                                    if (tab.isPrivate) PrivateViolet.copy(alpha = 0.16f) else tokens.base100,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            val brandIcon = if (tab.isPrivate) null else BrandIconLookup.iconResForUrl(tab.url)
                            if (brandIcon != null) {
                                Icon(
                                    painter = painterResource(id = brandIcon),
                                    contentDescription = null,
                                    tint = tokens.accentSolid,
                                    modifier = Modifier.size(18.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = if (tab.isPrivate) Icons.Filled.VisibilityOff else Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = if (tab.isPrivate) PrivateViolet else tokens.accentSolid,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = tab.displayTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.baseContent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                if (isSelected) {
                                    RadixBadge(
                                        text = "Active",
                                        variant = RadixBadgeVariant.SOLID,
                                        accentColor = if (tab.isPrivate) PrivateViolet else tokens.accentSolid,
                                    )
                                }
                            }
                            Text(
                                text = if (tab.url.isBlank()) "Start page" else UrlNormalizer.extractHost(tab.url),
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = tokens.mutedContent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(
                            onClick = { onCloseTab(idx) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_tab_$idx"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close_tab_cd),
                                tint = tokens.mutedContent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarksContent(
    bookmarks: List<BookmarkEntity>,
    onOpenUrl: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
) {
    val tokens = LocalOrbitTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("bookmarks_sheet"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.action_bookmarks),
                style = MaterialTheme.typography.titleLarge,
                color = tokens.baseContent,
            )
            if (bookmarks.isNotEmpty()) {
                RadixBadge(text = "${bookmarks.size}", variant = RadixBadgeVariant.SOFT)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (bookmarks.isEmpty()) {
            EmptySheetState(
                icon = Icons.Filled.Bookmarks,
                message = stringResource(R.string.empty_bookmarks_message),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(bookmarks, key = { it.url }) { item ->
                    RadixSurfaceCard(
                        onClick = { onOpenUrl(item.url) },
                        useElevatedTint = true,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = WarningAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.baseContent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = item.url,
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = tokens.mutedContent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(
                                onClick = { onRemoveBookmark(item.url) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    Icons.Filled.DeleteOutline,
                                    contentDescription = stringResource(R.string.bookmark_remove_cd),
                                    tint = tokens.mutedContent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryContent(
    history: List<HistoryEntity>,
    onOpenUrl: (String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearAll: () -> Unit,
) {
    val tokens = LocalOrbitTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("history_sheet"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.action_history),
                style = MaterialTheme.typography.titleLarge,
                color = tokens.baseContent,
            )
            if (history.isNotEmpty()) {
                TextButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.clear_history_button))
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (history.isEmpty()) {
            EmptySheetState(
                icon = Icons.Filled.History,
                message = stringResource(R.string.empty_history_message),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(history, key = { it.id }) { entry ->
                    RadixSurfaceCard(
                        onClick = { onOpenUrl(entry.url) },
                        useElevatedTint = true,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Public, contentDescription = null, tint = tokens.accentSolid)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.baseContent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = entry.url,
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = tokens.mutedContent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(
                                onClick = { onDeleteEntry(entry.id) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Delete visit",
                                    tint = tokens.mutedContent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsContent(
    settings: BrowserSettings,
    onSelectSearchEngine: (SearchEngine) -> Unit,
    onToggleJavaScript: (Boolean) -> Unit,
    onToggleDesktopDefault: (Boolean) -> Unit,
    onSelectThemePreset: (OrbitThemePreset) -> Unit,
    onSelectRadiusPreset: (OrbitRadiusPreset) -> Unit,
    onClearHistory: () -> Unit,
) {
    val tokens = LocalOrbitTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .testTag("settings_sheet"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.action_settings),
                style = MaterialTheme.typography.titleLarge,
                color = tokens.baseContent,
            )
            RadixBadge(
                text = settings.themePreset.displayName,
                variant = RadixBadgeVariant.SOFT,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // daisyUI + Radix Theme Swatch Picker
        Text(
            text = stringResource(R.string.settings_theme_preset),
            style = MaterialTheme.typography.titleMedium,
            color = tokens.baseContent,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OrbitThemePreset.entries.forEach { preset ->
                val isSelected = settings.themePreset == preset
                val chipShape = RoundedCornerShape(tokens.controlRadius)
                Row(
                    modifier = Modifier
                        .clip(chipShape)
                        .background(if (isSelected) tokens.accentSoft else tokens.base200)
                        .border(
                            width = if (isSelected) 1.6.dp else 1.dp,
                            color = if (isSelected) tokens.accentSolid else tokens.borderSubtle,
                            shape = chipShape,
                        )
                        .clickable { onSelectThemePreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("theme_preset_${preset.name.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(preset.previewSurface)
                            .border(2.dp, preset.previewPrimary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(preset.previewPrimary),
                        )
                    }
                    Text(
                        text = preset.displayName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) tokens.accentSoftText else tokens.baseContent,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Radix UI Corner Style Selector
        Text(
            text = stringResource(R.string.settings_corner_radius),
            style = MaterialTheme.typography.titleMedium,
            color = tokens.baseContent,
        )
        Spacer(modifier = Modifier.height(8.dp))
        DaisyJoinSegmentedControl(
            items = OrbitRadiusPreset.entries,
            selectedItem = settings.radiusPreset,
            labelFor = { it.displayName },
            tagFor = { "radius_preset_${it.name.lowercase()}" },
            onSelect = onSelectRadiusPreset,
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = tokens.borderSubtle)
        Spacer(modifier = Modifier.height(14.dp))

        // Search Engine Picker with Simple Icons
        Text(
            text = stringResource(R.string.settings_search_engine),
            style = MaterialTheme.typography.titleMedium,
            color = tokens.baseContent,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SearchEngine.entries.forEach { engine ->
                val selected = settings.searchEngine == engine
                val chipShape = RoundedCornerShape(tokens.controlRadius)
                Row(
                    modifier = Modifier
                        .clip(chipShape)
                        .background(if (selected) tokens.accentSoft else tokens.base200)
                        .border(
                            width = if (selected) 1.6.dp else 1.dp,
                            color = if (selected) tokens.accentSolid else tokens.borderSubtle,
                            shape = chipShape,
                        )
                        .clickable { onSelectSearchEngine(engine) }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("engine_chip_${engine.name.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        painter = painterResource(id = engine.iconRes),
                        contentDescription = null,
                        tint = engine.brandColor,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = engine.displayName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (selected) tokens.accentSoftText else tokens.baseContent,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Radix Surface Card for Toggles
        RadixSurfaceCard(
            useElevatedTint = true,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        stringResource(R.string.settings_javascript),
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.baseContent,
                    )
                    Text(
                        stringResource(R.string.settings_javascript_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.mutedContent,
                    )
                }
                Switch(
                    checked = settings.javaScriptEnabled,
                    onCheckedChange = onToggleJavaScript,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = tokens.accentSolid,
                        checkedThumbColor = if (tokens.isDark) Color(0xFF0B0E17) else Color.White,
                    ),
                    modifier = Modifier.testTag("settings_js_switch"),
                )
            }

            HorizontalDivider(
                color = tokens.borderSubtle,
                modifier = Modifier.padding(horizontal = 14.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        stringResource(R.string.settings_desktop_default),
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.baseContent,
                    )
                    Text(
                        stringResource(R.string.settings_desktop_default_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.mutedContent,
                    )
                }
                Switch(
                    checked = settings.desktopModeDefault,
                    onCheckedChange = onToggleDesktopDefault,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = tokens.accentSolid,
                        checkedThumbColor = if (tokens.isDark) Color(0xFF0B0E17) else Color.White,
                    ),
                    modifier = Modifier.testTag("settings_desktop_switch"),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onClearHistory,
            shape = RoundedCornerShape(tokens.controlRadius),
            border = BorderStroke(1.dp, tokens.borderStrong),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text(stringResource(R.string.clear_history_button))
        }
    }
}

@Composable
private fun EmptySheetState(
    icon: ImageVector,
    message: String,
) {
    val tokens = LocalOrbitTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.mutedContent.copy(alpha = 0.6f),
                modifier = Modifier.size(44.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = tokens.mutedContent,
                textAlign = TextAlign.Center,
            )
        }
    }
}
