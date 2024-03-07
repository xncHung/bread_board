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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import io.xnchung.breadboard.model.SampleContract

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CatalogScreen(
    samples: List<SampleContract>,
    settings: CatalogSettings,
    launchSample: SampleContract.() -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var searchState by rememberSaveable {
        mutableStateOf(false)
    }
    var searchKeyword by rememberSaveable(searchState) {
        mutableStateOf("")
    }
    val filters by remember(samples, settings) {
        derivedStateOf {
            settings.filters.map { filter ->
                filter.fromSamples(samples)
            }
        }
    }
    val selectedFilters = rememberSaveable(
        saver = listSaver(
            save = {
                it.toList()
            },
            restore = {
                it.toMutableStateList()
            }
        )
    ) {
        MutableList(filters.size) { emptyArray<CatalogFilter>() }.toMutableStateList()
    }
    val displayedSamples by remember(searchKeyword, selectedFilters) {
        derivedStateOf {
            samples.filter { sample ->
                selectedFilters.all {
                    it.isEmpty() || it.any {
                        it.accept(sample)
                    }
                }
            }.filter { sample ->
                searchKeyword.isBlank() ||
                        sample.name.contains(searchKeyword, ignoreCase = true) ||
                        sample.description.contains(searchKeyword, ignoreCase = true) ||
                        sample.tags.any { it.equals(searchKeyword, ignoreCase = true) }
            }
        }
    }

    val saver = Saver<SampleContract?, String>(
        save = { it?.route.orEmpty() },
        restore = { item -> displayedSamples.find { it.route == item } }
    )
    var selectedSample by rememberSaveable(stateSaver = saver) {
        mutableStateOf(null)
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                label = "Top app bar",
                targetState = searchState,
                transitionSpec = {
                    if (targetState) {
                        val enterTransition = expandHorizontally { it / 8 } + fadeIn(tween(100))
                        ContentTransform(enterTransition, fadeOut())
                    } else {
                        val fadeIn = fadeIn(tween(500, delayMillis = 100))
                        val exitTransition = shrinkHorizontally(tween(400)) { it / 8 } + fadeOut(tween(400))
                        ContentTransform(fadeIn, exitTransition)
                    }
                },
                contentAlignment = Alignment.CenterEnd
            ) {
                if (it) {
                    SearchTopAppBar(
                        searchTerm = searchKeyword,
                        focusRequester = focusRequester,
                        onSearch = {
                            searchState = false
                        },
                        onClear = {
                            searchState = false
                        },
                        onValueChange = {
                            searchKeyword = it
                        }
                    )
                } else {
                    CatalogTopAppBar(
                        selectedSample = null,
                        onSearch = { searchState = true }
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        )
        {
            SamplesList(
                filters,
                selectedFilters,
                displayedSamples,
                selectedSample,
                settings.cardAppearance,
                it,
            ) {
                it.launchSample()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SamplesList(
    filtersList: List<List<CatalogFilter>>,
    selectedFilters: SnapshotStateList<Array<CatalogFilter>>,
    displayedSamples: List<SampleContract>,
    selectedSample: SampleContract?,
    appearance: CatalogCardAppearance,
    contentPadding: PaddingValues,
    launchSample: (SampleContract) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filtersList.forEachIndexed { index, filters ->
            val catalogFilters = selectedFilters[index]
            FilterTabRow(filters, catalogFilters) {
                selectedFilters[index] = if (catalogFilters.contains(it)) {
                    catalogFilters.toMutableList().apply {
                        remove(it)
                    }
                } else {
                    catalogFilters.toMutableList().apply {
                        add(it)
                    }
                }.toTypedArray()
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(displayedSamples, key = { it.id }) {
                CardItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(spring())
                        .padding(horizontal = 16.dp),
                    label = it.name,
                    appearance = appearance,
                    description = it.description,
                    tags = it.tags,
                    owners = it.owners,
                    selected = it == selectedSample
                ) {
                    launchSample(it)
                }
            }
        }
    }
}

internal inline fun <T> LazyListScope.items(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) = items(
    count = items.size,
    key = if (key != null) { index: Int -> key(items[index]) } else null,
    contentType = { index: Int -> contentType(items[index]) }
) {
    itemContent(items[it])
}