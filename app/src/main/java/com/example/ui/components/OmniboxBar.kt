package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BrandIconLookup
import com.example.model.BrowserTabState
import com.example.model.SearchEngine
import com.example.ui.OmniboxSuggestion
import com.example.ui.SuggestionKind
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalOrbitTokens
import com.example.ui.theme.PrivateViolet
import com.example.ui.theme.SecureEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun OmniboxBar(
    tab: BrowserTabState,
    omniboxText: String,
    isFocused: Boolean,
    onTextChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onReloadOrStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalOrbitTokens.current
    val focusManager = LocalFocusManager.current

    fun submitAndUnfocus() {
        onSubmit()
        focusManager.clearFocus()
    }

    val targetBorderColor = when {
        isFocused && tab.isPrivate -> PrivateViolet
        isFocused -> tokens.accentSolid
        tab.isPrivate -> PrivateViolet.copy(alpha = 0.58f)
        else -> tokens.borderStrong
    }
    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(200),
        label = "omnibox_border_color",
    )
    val fillBg by animateColorAsState(
        targetValue = if (isFocused) tokens.base100 else tokens.base200,
        animationSpec = tween(200),
        label = "omnibox_bg_color",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(tokens.controlRadius),
            color = fillBg,
            border = BorderStroke(if (isFocused) 1.6.dp else 1.dp, borderColor),
            tonalElevation = 0.dp,
            shadowElevation = if (isFocused) 3.dp else 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val badgeBg = when {
                    tab.isPrivate -> PrivateViolet.copy(alpha = 0.16f)
                    tab.isStartPage -> tokens.accentSoft
                    tab.isSecure -> SecureEmerald.copy(alpha = 0.15f)
                    else -> WarningAmber.copy(alpha = 0.16f)
                }
                val badgeShape = RoundedCornerShape((tokens.controlRadius * 0.6f).coerceAtLeast(6.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(badgeShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        tab.isPrivate -> Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = stringResource(R.string.private_tab_cd),
                            tint = PrivateViolet,
                            modifier = Modifier.size(16.dp),
                        )
                        tab.isStartPage -> Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = tokens.accentSolid,
                            modifier = Modifier.size(16.dp),
                        )
                        tab.isSecure -> Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.secure_site_cd),
                            tint = SecureEmerald,
                            modifier = Modifier.size(15.dp),
                        )
                        else -> Icon(
                            imageVector = Icons.Outlined.LockOpen,
                            contentDescription = stringResource(R.string.insecure_site_cd),
                            tint = WarningAmber,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (omniboxText.isEmpty()) {
                        Text(
                            text = stringResource(R.string.omnibox_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = tokens.mutedContent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    BasicTextField(
                        value = omniboxText,
                        onValueChange = { raw ->
                            if (raw.contains('\n') || raw.contains('\r')) {
                                val cleaned = raw.replace("\n", "").replace("\r", "")
                                onTextChange(cleaned)
                                if (cleaned.isNotBlank()) {
                                    submitAndUnfocus()
                                }
                            } else {
                                onTextChange(raw)
                            }
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = if (omniboxText.contains(".") && !isFocused) {
                                JetBrainsMonoFontFamily
                            } else {
                                MaterialTheme.typography.bodyLarge.fontFamily
                            },
                            color = tokens.baseContent,
                        ),
                        cursorBrush = SolidColor(if (tab.isPrivate) PrivateViolet else tokens.accentSolid),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go,
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = { submitAndUnfocus() },
                            onSearch = { submitAndUnfocus() },
                            onDone = { submitAndUnfocus() },
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) {
                                    if (keyEvent.type == KeyEventType.KeyDown && omniboxText.isNotBlank()) {
                                        submitAndUnfocus()
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                            .onFocusChanged { focusState ->
                                onFocusChange(focusState.isFocused)
                            }
                            .testTag("omnibox_input"),
                    )
                }

                if (tab.isDesktopMode && !isFocused) {
                    Surface(
                        shape = RoundedCornerShape((tokens.controlRadius * 0.55f).coerceAtLeast(6.dp)),
                        color = tokens.accentSoft,
                        border = BorderStroke(0.8.dp, tokens.accentBorder),
                        modifier = Modifier.padding(end = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DesktopWindows,
                            contentDescription = stringResource(R.string.action_desktop_site),
                            tint = tokens.accentSoftText,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(14.dp),
                        )
                    }
                }

                if (isFocused && omniboxText.isNotEmpty()) {
                    IconButton(
                        onClick = { onTextChange("") },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("omnibox_clear_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.clear_url_cd),
                            tint = tokens.mutedContent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(
                        onClick = { submitAndUnfocus() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("omnibox_go_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go",
                            tint = tokens.accentSolid,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                } else if (!tab.isStartPage) {
                    IconButton(
                        onClick = onReloadOrStop,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("omnibox_reload_button"),
                    ) {
                        Icon(
                            imageVector = if (tab.isLoading) Icons.Filled.Close else Icons.Filled.Refresh,
                            contentDescription = if (tab.isLoading) {
                                stringResource(R.string.stop_loading_cd)
                            } else {
                                stringResource(R.string.reload_page_cd)
                            },
                            tint = tokens.baseContent,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = tab.isLoading && tab.progress in 1..99) {
            val animatedFraction by animateFloatAsState(
                targetValue = (tab.progress / 100f).coerceIn(0f, 1f),
                animationSpec = tween(180),
                label = "omnibox_progress",
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 8.dp, end = 8.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(tokens.base300),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = if (tab.isPrivate) {
                                    listOf(PrivateViolet, tokens.secondarySolid)
                                } else {
                                    listOf(tokens.accentSolid, tokens.secondarySolid)
                                },
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
fun OmniboxSuggestionsDropdown(
    suggestions: List<OmniboxSuggestion>,
    searchEngine: SearchEngine,
    onSelectSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty()) return
    val tokens = LocalOrbitTokens.current

    RadixSurfaceCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("omnibox_suggestions"),
    ) {
        suggestions.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSuggestion(item.url) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("suggestion_item_$index"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val iconShape = RoundedCornerShape((tokens.controlRadius * 0.65f).coerceAtLeast(6.dp))
                val isSearch = item.kind == SuggestionKind.SEARCH_ENGINE
                val isBookmark = item.kind == SuggestionKind.BOOKMARK

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(iconShape)
                        .background(
                            when {
                                isBookmark -> WarningAmber.copy(alpha = 0.14f)
                                isSearch -> searchEngine.brandColor.copy(alpha = 0.14f)
                                else -> tokens.accentSoft
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSearch) {
                        Icon(
                            painter = painterResource(id = searchEngine.iconRes),
                            contentDescription = null,
                            tint = searchEngine.brandColor,
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        val brandIcon = BrandIconLookup.iconResForUrl(item.url)
                        if (brandIcon != null) {
                            Icon(
                                painter = painterResource(id = brandIcon),
                                contentDescription = null,
                                tint = if (isBookmark) WarningAmber else tokens.accentSolid,
                                modifier = Modifier.size(18.dp),
                            )
                        } else {
                            Icon(
                                imageVector = if (isBookmark) Icons.Filled.Star else Icons.Filled.History,
                                contentDescription = null,
                                tint = if (isBookmark) WarningAmber else tokens.accentSolid,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.baseContent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.subtitle,
                        style = if (isSearch) {
                            MaterialTheme.typography.labelMedium
                        } else {
                            MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily)
                        },
                        color = tokens.mutedContent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RadixBadge(
                    text = when (item.kind) {
                        SuggestionKind.BOOKMARK -> "Saved"
                        SuggestionKind.HISTORY -> "Visit"
                        SuggestionKind.SEARCH_ENGINE -> searchEngine.displayName
                    },
                    variant = RadixBadgeVariant.SOFT,
                    accentColor = when (item.kind) {
                        SuggestionKind.BOOKMARK -> WarningAmber
                        SuggestionKind.HISTORY -> tokens.accentSolid
                        SuggestionKind.SEARCH_ENGINE -> searchEngine.brandColor
                    },
                    softBgColor = when (item.kind) {
                        SuggestionKind.BOOKMARK -> WarningAmber.copy(alpha = 0.14f)
                        SuggestionKind.HISTORY -> tokens.accentSoft
                        SuggestionKind.SEARCH_ENGINE -> searchEngine.brandColor.copy(alpha = 0.14f)
                    },
                    softTextColor = when (item.kind) {
                        SuggestionKind.BOOKMARK -> WarningAmber
                        SuggestionKind.HISTORY -> tokens.accentSoftText
                        SuggestionKind.SEARCH_ENGINE -> searchEngine.brandColor
                    },
                )
            }
            if (index < suggestions.lastIndex) {
                HorizontalDivider(
                    color = tokens.borderSubtle,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }
        }
    }
}
