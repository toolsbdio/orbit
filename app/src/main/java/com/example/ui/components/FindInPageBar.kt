package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalOrbitTokens

@Composable
fun FindInPageBar(
    query: String,
    activeMatch: Int,
    totalMatches: Int,
    onQueryChange: (String) -> Unit,
    onFindPrev: () -> Unit,
    onFindNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalOrbitTokens.current

    Surface(
        color = tokens.base100,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("find_in_page_bar"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = tokens.borderSubtle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(tokens.controlRadius),
                    color = tokens.base200,
                    border = BorderStroke(1.dp, tokens.borderStrong),
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = tokens.baseContent,
                            ),
                            cursorBrush = SolidColor(tokens.accentSolid),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onFindNext() }),
                            decorationBox = { inner ->
                                if (query.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.find_placeholder),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = tokens.mutedContent,
                                    )
                                }
                                inner()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("find_in_page_input"),
                        )

                        if (query.isNotEmpty()) {
                            RadixKbdPill(
                                text = if (totalMatches > 0) "$activeMatch/$totalMatches" else "0/0",
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onFindPrev,
                    enabled = totalMatches > 0,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.find_prev_cd),
                        tint = if (totalMatches > 0) tokens.baseContent else tokens.mutedContent.copy(alpha = 0.45f),
                    )
                }

                IconButton(
                    onClick = onFindNext,
                    enabled = totalMatches > 0,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.find_next_cd),
                        tint = if (totalMatches > 0) tokens.baseContent else tokens.mutedContent.copy(alpha = 0.45f),
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("find_in_page_close"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.find_close_cd),
                        tint = tokens.mutedContent,
                    )
                }
            }
        }
    }
}
