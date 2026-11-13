package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalOrbitTokens

enum class RadixBadgeVariant {
    SOLID,
    SOFT,
    OUTLINE,
}

@Composable
fun RadixBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: RadixBadgeVariant = RadixBadgeVariant.SOFT,
    accentColor: Color = LocalOrbitTokens.current.accentSolid,
    softBgColor: Color = LocalOrbitTokens.current.accentSoft,
    softTextColor: Color = LocalOrbitTokens.current.accentSoftText,
    leadingIcon: ImageVector? = null,
) {
    val tokens = LocalOrbitTokens.current
    val bgColor = when (variant) {
        RadixBadgeVariant.SOLID -> accentColor
        RadixBadgeVariant.SOFT -> softBgColor
        RadixBadgeVariant.OUTLINE -> Color.Transparent
    }
    val textColor = when (variant) {
        RadixBadgeVariant.SOLID -> if (tokens.isDark) Color(0xFF0B0E17) else Color.White
        RadixBadgeVariant.SOFT -> softTextColor
        RadixBadgeVariant.OUTLINE -> accentColor
    }
    val border = if (variant == RadixBadgeVariant.OUTLINE) {
        BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
    } else {
        BorderStroke(0.5.dp, accentColor.copy(alpha = 0.16f))
    }

    Surface(
        shape = RoundedCornerShape(tokens.controlRadius * 0.65f),
        color = bgColor,
        border = border,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(13.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RadixSurfaceCard(
    modifier: Modifier = Modifier,
    useElevatedTint: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = LocalOrbitTokens.current
    val shape = RoundedCornerShape(tokens.cardRadius)
    val bg = if (useElevatedTint) tokens.base300.copy(alpha = 0.6f) else tokens.base100

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = bg,
            border = BorderStroke(1.dp, tokens.borderSubtle),
            tonalElevation = 0.dp,
            modifier = modifier,
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            shape = shape,
            color = bg,
            border = BorderStroke(1.dp, tokens.borderSubtle),
            tonalElevation = 0.dp,
            modifier = modifier,
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun <T> DaisyJoinSegmentedControl(
    items: List<T>,
    selectedItem: T,
    labelFor: (T) -> String,
    tagFor: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalOrbitTokens.current
    val outerShape = RoundedCornerShape(tokens.controlRadius + 2.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(tokens.base300.copy(alpha = 0.72f))
            .border(1.dp, tokens.borderSubtle, outerShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            val itemShape = RoundedCornerShape(tokens.controlRadius - 2.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(itemShape)
                    .background(if (isSelected) tokens.base100 else Color.Transparent)
                    .then(
                        if (isSelected) {
                            Modifier.border(1.dp, tokens.borderSubtle, itemShape)
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelect(item) }
                    .testTag(tagFor(item)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = labelFor(item),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isSelected) tokens.baseContent else tokens.mutedContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun RadixKbdPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalOrbitTokens.current
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = tokens.base200,
        border = BorderStroke(1.dp, tokens.borderStrong),
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tokens.mutedContent,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}
