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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.data.local.BookmarkEntity
import com.example.data.local.HistoryEntity
import com.example.model.BrandIconLookup
import com.example.model.DefaultSpeedDial
import com.example.model.QuickLink
import com.example.model.UrlNormalizer
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalOrbitTokens
import com.example.ui.theme.PrivatePlumDeep
import com.example.ui.theme.PrivateViolet
import com.example.ui.theme.SecureEmerald
import com.example.ui.theme.WarningAmber

private enum class StartPageSection {
    QUICK_ACCESS,
    BOOKMARKS,
    HISTORY,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrowserStartPage(
    isPrivate: Boolean,
    bookmarks: List<BookmarkEntity>,
    recentHistory: List<HistoryEntity>,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalOrbitTokens.current
    var selectedSection by rememberSaveable { mutableStateOf(StartPageSection.QUICK_ACCESS) }
    val primaryGlow = if (isPrivate) PrivateViolet else tokens.accentSolid
    val secondaryGlow = if (isPrivate) tokens.accentSolid else tokens.secondarySolid

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.base200)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryGlow.copy(alpha = if (tokens.isDark) 0.16f else 0.10f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.22f, size.height * 0.10f),
                        radius = size.maxDimension * 0.55f,
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryGlow.copy(alpha = if (tokens.isDark) 0.12f else 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.28f),
                        radius = size.maxDimension * 0.48f,
                    ),
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isPrivate) {
                Surface(
                    shape = RoundedCornerShape(tokens.cardRadius),
                    color = PrivatePlumDeep,
                    border = BorderStroke(1.2.dp, PrivateViolet.copy(alpha = 0.7f)),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("private_mode_banner"),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A0C2E))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(PrivateViolet))
                                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(tokens.secondarySolid))
                                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(SecureEmerald))
                            }
                            RadixBadge(
                                text = "Private Mode",
                                variant = RadixBadgeVariant.SOFT,
                                accentColor = PrivateViolet,
                                softBgColor = PrivateViolet.copy(alpha = 0.28f),
                                softTextColor = Color(0xFFEBDFFF),
                                leadingIcon = Icons.Filled.Shield,
                            )
                        }
                        HorizontalDivider(color = PrivateViolet.copy(alpha = 0.35f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(tokens.controlRadius))
                                    .background(PrivateViolet.copy(alpha = 0.32f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.private_tab_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = stringResource(R.string.private_banner_text),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFE0D7FE),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // daisyUI Join Segmented Switcher
            DaisyJoinSegmentedControl(
                items = StartPageSection.entries,
                selectedItem = selectedSection,
                labelFor = { section ->
                    when (section) {
                        StartPageSection.QUICK_ACCESS -> "Quick Access"
                        StartPageSection.BOOKMARKS -> "Bookmarks (${bookmarks.size})"
                        StartPageSection.HISTORY -> "History"
                    }
                },
                tagFor = { "start_tab_${it.name.lowercase()}" },
                onSelect = { selectedSection = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedSection) {
                StartPageSection.QUICK_ACCESS -> {
                    RadixSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.speed_dial_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = tokens.baseContent,
                            )
                            RadixBadge(
                                text = "${DefaultSpeedDial.links.size} sites",
                                variant = RadixBadgeVariant.SOFT,
                            )
                        }
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 4,
                        ) {
                            DefaultSpeedDial.links.forEach { link ->
                                SpeedDialTile(
                                    link = link,
                                    onClick = { onOpenUrl(link.url) },
                                    modifier = Modifier.width(68.dp),
                                )
                            }
                        }
                    }
                }

                StartPageSection.BOOKMARKS -> {
                    if (bookmarks.isEmpty()) {
                        EmptySectionCard(
                            icon = Icons.Filled.Bookmarks,
                            title = stringResource(R.string.bookmarks_section_title),
                            message = stringResource(R.string.empty_bookmarks_message),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            bookmarks.take(8).forEach { bookmark ->
                                val brandIcon = BrandIconLookup.iconResForUrl(bookmark.url)
                                SavedSiteRow(
                                    title = bookmark.title,
                                    url = bookmark.url,
                                    badgeText = "Saved",
                                    badgeColor = WarningAmber,
                                    leadingIcon = {
                                        if (brandIcon != null) {
                                            Icon(
                                                painter = painterResource(id = brandIcon),
                                                contentDescription = null,
                                                tint = WarningAmber,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = WarningAmber,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    },
                                    onClick = { onOpenUrl(bookmark.url) },
                                )
                            }
                        }
                    }
                }

                StartPageSection.HISTORY -> {
                    val visibleHistory = if (isPrivate) emptyList() else recentHistory.distinctBy { it.url }
                    if (visibleHistory.isEmpty()) {
                        EmptySectionCard(
                            icon = if (isPrivate) Icons.Filled.VisibilityOff else Icons.Filled.History,
                            title = stringResource(R.string.recent_history_title),
                            message = if (isPrivate) {
                                stringResource(R.string.private_banner_text)
                            } else {
                                stringResource(R.string.empty_history_message)
                            },
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            visibleHistory.take(8).forEach { entry ->
                                val brandIcon = BrandIconLookup.iconResForUrl(entry.url)
                                SavedSiteRow(
                                    title = entry.title,
                                    url = entry.url,
                                    badgeText = "Recent",
                                    badgeColor = tokens.accentSolid,
                                    leadingIcon = {
                                        if (brandIcon != null) {
                                            Icon(
                                                painter = painterResource(id = brandIcon),
                                                contentDescription = null,
                                                tint = tokens.accentSolid,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.Public,
                                                contentDescription = null,
                                                tint = tokens.accentSolid,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    },
                                    onClick = { onOpenUrl(entry.url) },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SpeedDialTile(
    link: QuickLink,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalOrbitTokens.current
    val tileShape = RoundedCornerShape(tokens.controlRadius)

    Column(
        modifier = modifier
            .clip(tileShape)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .testTag("speed_dial_${link.title.lowercase().replace(" ", "_")}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(tileShape)
                .background(link.accentColor.copy(alpha = if (tokens.isDark) 0.20f else 0.12f))
                .border(1.dp, link.accentColor.copy(alpha = 0.30f), tileShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = link.iconRes),
                contentDescription = link.title,
                tint = link.accentColor,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = link.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = tokens.baseContent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SavedSiteRow(
    title: String,
    url: String,
    badgeText: String,
    badgeColor: Color,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    val tokens = LocalOrbitTokens.current

    RadixSurfaceCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape((tokens.controlRadius * 0.75f).coerceAtLeast(6.dp)))
                    .background(badgeColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                leadingIcon()
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.baseContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = UrlNormalizer.extractHost(url),
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = tokens.mutedContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RadixBadge(
                text = badgeText,
                variant = RadixBadgeVariant.SOFT,
                accentColor = badgeColor,
                softBgColor = badgeColor.copy(alpha = 0.14f),
                softTextColor = badgeColor,
            )
        }
    }
}

@Composable
private fun EmptySectionCard(
    icon: ImageVector,
    title: String,
    message: String,
) {
    val tokens = LocalOrbitTokens.current

    RadixSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(tokens.controlRadius))
                    .background(tokens.accentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tokens.accentSoftText,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = tokens.baseContent,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.mutedContent,
                textAlign = TextAlign.Center,
            )
        }
    }
}
