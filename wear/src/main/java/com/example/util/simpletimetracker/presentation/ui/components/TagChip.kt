/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.CheckboxDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.SplitToggleChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.tooling.preview.devices.WearDevices

@Immutable
data class TagChipState(
    val id: Long,
    val name: String,
    val color: Long,
    val checked: Boolean,
    val mode: TagSelectionMode,
    val isLoading: Boolean = false,
) {
    enum class TagSelectionMode {
        SINGLE,
        MULTI,
    }
}

@Composable
fun TagChip(
    state: TagChipState,
    onClick: (Long) -> Unit = {},
) {
    when (state.mode) {
        TagChipState.TagSelectionMode.SINGLE -> {
            SingleSelectTagChip(
                state = state,
                onClick = onClick,
            )
        }

        TagChipState.TagSelectionMode.MULTI -> {
            MultiSelectTagChip(
                state = state,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun SingleSelectTagChip(
    state: TagChipState,
    onClick: (Long) -> Unit,
) {
    val onClickState = remember(state.id) {
        { onClick(state.id) }
    }
    val height = ACTIVITY_VIEW_HEIGHT.dp *
        LocalDensity.current.fontScale
    Chip(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        onClick = onClickState,
        label = {
            if (!state.isLoading) {
                Text(
                    text = state.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxHeight(0.7f),
                )
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color(state.color),
        ),
    )
}

@Composable
private fun MultiSelectTagChip(
    state: TagChipState,
    onClick: (Long) -> Unit = {},
) {
    val onCheckedChange: (Boolean) -> Unit = remember(state.id) {
        { onClick(state.id) }
    }
    val onClickState = remember(state.id) {
        { onClick(state.id) }
    }
    val height = ACTIVITY_VIEW_HEIGHT.dp *
        LocalDensity.current.fontScale
    SplitToggleChip(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        checked = state.checked,
        onCheckedChange = onCheckedChange,
        onClick = onClickState,
        label = {
            if (!state.isLoading) {
                Text(
                    text = state.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxHeight(0.7f),
                )
            }
        },
        toggleControl = {
            Checkbox(
                checked = state.checked,
                colors = CheckboxDefaults.colors(
                    checkedBoxColor = Color.White,
                    checkedCheckmarkColor = Color.White,
                    uncheckedBoxColor = Color.White,
                    uncheckedCheckmarkColor = Color.White,
                ),
            )
        },
        colors = ToggleChipDefaults.splitToggleChipColors(
            backgroundColor = Color(state.color),
            splitBackgroundOverlayColor = if (state.checked) {
                Color.White.copy(alpha = .1F)
            } else {
                Color.Black.copy(alpha = .3F)
            },
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Default() {
    TagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF123456,
            checked = false,
            mode = TagChipState.TagSelectionMode.SINGLE,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun DefaultWithFontScale() {
    TagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF123456,
            checked = false,
            mode = TagChipState.TagSelectionMode.SINGLE,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Loading() {
    TagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF123456,
            checked = false,
            mode = TagChipState.TagSelectionMode.SINGLE,
            isLoading = true,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectMode() {
    TagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF654321,
            checked = false,
            mode = TagChipState.TagSelectionMode.MULTI,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun MultiSelectModeFontScale() {
    TagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF654321,
            checked = false,
            mode = TagChipState.TagSelectionMode.MULTI,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectChecked() {
    MultiSelectTagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF654321,
            checked = true,
            mode = TagChipState.TagSelectionMode.MULTI,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectLoading() {
    MultiSelectTagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF654321,
            checked = false,
            mode = TagChipState.TagSelectionMode.MULTI,
            isLoading = true,
        ),
    )
}