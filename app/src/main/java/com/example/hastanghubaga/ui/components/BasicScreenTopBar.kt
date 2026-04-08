package com.example.hastanghubaga.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class BasicTopBarAction(
    val label: String,
    val onClick: () -> Unit,
    val isEnabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicScreenTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    overflowActions: List<BasicTopBarAction> = emptyList()
) {
    var isOverflowExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (overflowActions.isNotEmpty()) {
                IconButton(
                    onClick = { isOverflowExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More actions"
                    )
                }

                DropdownMenu(
                    expanded = isOverflowExpanded,
                    onDismissRequest = { isOverflowExpanded = false }
                ) {
                    overflowActions.forEach { action ->
                        DropdownMenuItem(
                            text = { Text(action.label) },
                            onClick = {
                                isOverflowExpanded = false
                                action.onClick()
                            },
                            enabled = action.isEnabled
                        )
                    }
                }
            }
        }
    )
}
