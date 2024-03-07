/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xnchung.breadboard.launcher.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import io.xnchung.breadboard.model.SampleContract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTopAppBar(
    selectedSample: SampleContract? = null,
    onSearch: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    fun launchUrl(url: String) {
        check(url.isNotBlank()) {
            "Provided URL is empty"
        }
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        menuExpanded = false
    }

    TopAppBar(
        title = {
            Text(
                text = selectedSample?.name ?: "BreadBoard",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            Box {
                Row(Modifier.animateContentSize()) {
                    if (selectedSample == null) {
                        IconButton(onClick = onSearch) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search"
                            )
                        }
                    }

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert, contentDescription = "Open menu"
                        )
                    }
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        onClick = { launchUrl("") },
                        text = { Text(text = "Source code") }
                    )

//                    val docsUrl = if (selectedSample != null) {
//                        if (selectedSample.documentation.startsWith("http")) {
//                            selectedSample.documentation
//                        } else {
//                            stringResource(
//                                id = R.string.documentation_base_url, selectedSample.documentation
//                            )
//                        }
//                    } else {
//                        stringResource(R.string.documentation_base_url)
//                    }
                    DropdownMenuItem(
                        onClick = { launchUrl("") },
                        text = { Text(text = "Documentation") }
                    )
                }
            }
        },
        navigationIcon = {
            if (selectedSample != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
            }
        },
    )
}
