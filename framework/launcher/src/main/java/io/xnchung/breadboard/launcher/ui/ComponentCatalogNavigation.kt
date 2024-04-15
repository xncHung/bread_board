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

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.xnchung.breadboard.converters.ConverterRepo
import io.xnchung.breadboard.converters.getTypeParameterForParamHolder
import io.xnchung.breadboard.launcher.Constants
import io.xnchung.breadboard.launcher.ui.model.NavArgTypeWrapper
import io.xnchung.breadboard.launcher.ui.model.ParamDraft
import io.xnchung.breadboard.model.ParamHolderContract
import io.xnchung.breadboard.model.SampleContract
import io.xnchung.breadboard.model.Target
import kotlinx.coroutines.launch

@Composable
internal fun ComponentCatalogNavigation(
    startDestination: String,
    samples: List<SampleContract>,
    settings: CatalogSettings,
    fragmentManager: FragmentManager
) {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = startDestination
    ) {
        // Add the home destination
        composable(Constants.CATALOG_HOME) {
            var selectedSample by remember {
                mutableStateOf<SampleContract?>(null)
            }
            var showConverterLoading by remember {
                mutableStateOf(false)
            }
            CatalogScreen(samples, settings) {

                when (target) {
                    is Target.TargetActivity,
                    is Target.TargetActivityJava -> {
                        selectedSample = this
                    }

                    is Target.TargetCommonFunc -> TODO()
                    is Target.TargetCommonJavaFunc -> TODO()
                    is Target.TargetCommonJavaFuncVoid -> TODO()
                    is Target.TargetComposable -> TODO()
                    is Target.TargetComposableJava -> TODO()
                    is Target.TargetDialog -> TODO()
                    is Target.TargetDialogJava -> TODO()
                    is Target.TargetDialogWithBuildFunc<*> -> TODO()
                    is Target.TargetDialogWithJavaBuildFunc<*> -> TODO()
                    is Target.TargetFragment -> TODO()
                    is Target.TargetFragmentJava -> TODO()
                    is Target.TargetFragmentWithBuildFunc<*> -> TODO()
                    is Target.TargetFragmentWithJavaBuildFunc<*> -> TODO()
                    is Target.TargetView -> TODO()
                    is Target.TargetViewJava -> TODO()
                    is Target.TargetViewWithBuildFunc<*> -> TODO()
                    is Target.TargetViewWithJavaBuildFunc<*> -> TODO()
                }
            }
            selectedSample?.apply {
                val items = paramHolders.map { ParamDraft(it) }
                Dialog(onDismissRequest = {}) {
                    val scope = rememberCoroutineScope()
                    Column {
                        Text(text = "Fill Args")

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            content = {
                                items(items = items, key = { it.key }, itemContent = { paramDraft: ParamDraft<Any?> ->
                                    TextField(
                                        value = paramDraft.input.value,
                                        onValueChange = {
                                            paramDraft.input.value = it
                                        },
                                        placeholder = { Text(text = paramDraft.desc) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                })
                            })

                        Row {
                            Button(onClick = {
                                scope.launch {
                                    showConverterLoading = true
                                    items.forEach {
                                        ConverterRepo.parseArg(this@apply, it, it.input.value)
                                    }
                                    val builder = Uri.parse(route).buildUpon()
                                    val uri = items.fold(builder) { builder: Uri.Builder, paramDraft: ParamDraft<Any?> ->
                                        builder.appendQueryParameter(paramDraft.key, paramDraft.input.value)
                                    }.build()
                                    navController.navigate(uri.toString())
                                    showConverterLoading = false
                                    selectedSample = null
                                }
                            }) {
                                Text(text = "Confirm")
                            }
                            Button(onClick = { selectedSample = null }) {
                                Text(text = "Cancel")
                            }
                        }
                    }
                }
            }
            if (showConverterLoading) {
                LoadingDialog()
            }
        }

        // Add all the samples
        samples.forEach { sample ->
            addTargets(
                sample = sample,
                fragmentManager = fragmentManager,
                settings = settings,
                onBackClick = navController::popBackStack
            )
        }
    }
}

private fun NavGraphBuilder.addTargets(
    sample: SampleContract,
    fragmentManager: FragmentManager,
    settings: CatalogSettings,
    onBackClick: () -> Unit,
) {
    val sampleRoute =
        sample.route + sample.paramHolders.joinToString(
            separator = "&",
            prefix = if (sample.route.contains("?")) "" else "?"
        ) { "${it.key}={${it.key}}" }
    when (val target = sample.target) {
        is Target.TargetComposable -> {
            composable(sampleRoute) {
                SampleScaffold(
                    sample = sample,
                    settings = settings,
                    onBackClick = onBackClick,
                ) {
                    target.composable(emptyMap())
                }
            }
        }

        is Target.TargetFragment -> {
            composable(sampleRoute) {
                SampleScaffold(
                    sample = sample,
                    settings = settings,
                    onBackClick = onBackClick
                ) {
                    FragmentContainer(
                        modifier = Modifier.fillMaxSize(),
                        fragmentManager = fragmentManager,
                        createFragment = {
                            target.targetClass.java.getDeclaredConstructor().newInstance()
                        },
                    )
                }
            }
        }

        is Target.TargetActivity -> {
            activity(sampleRoute) {
                label = sample.name
                activityClass = target.targetClass
                sample.paramHolders.forEach { holderContract: ParamHolderContract<*> ->
                    argument(holderContract.key) {
                        type = NavArgTypeWrapper(holderContract.getTypeParameterForParamHolder(), sample, holderContract)
                        nullable = holderContract.nullable
                    }
                }
            }
        }

        is Target.TargetActivityJava -> {
            activity(sampleRoute) {
                label = sample.name
                activityClass = target.targetClass.kotlin
                sample.paramHolders.forEach { holderContract: ParamHolderContract<*> ->
                    argument(holderContract.key) {
                        type = NavArgTypeWrapper(holderContract.getTypeParameterForParamHolder(), sample, holderContract)
                        nullable = holderContract.nullable
                    }
                }
            }
        }

        is Target.TargetCommonFunc -> {}
        is Target.TargetCommonJavaFunc -> {}
        is Target.TargetCommonJavaFuncVoid -> {}
        is Target.TargetComposableJava -> {}
        is Target.TargetDialog -> {}
        is Target.TargetDialogJava -> {}
        is Target.TargetDialogWithBuildFunc<*> -> {}
        is Target.TargetDialogWithJavaBuildFunc<*> -> {}
        is Target.TargetFragmentJava -> {}
        is Target.TargetFragmentWithBuildFunc<*> -> {}
        is Target.TargetFragmentWithJavaBuildFunc<*> -> {}
        is Target.TargetView -> {}
        is Target.TargetViewJava -> {}
        is Target.TargetViewWithBuildFunc<*> -> {}
        is Target.TargetViewWithJavaBuildFunc<*> -> {}
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SampleScaffold(
    sample: SampleContract,
    settings: CatalogSettings,
    onBackClick: () -> Unit,
    content: @Composable() (BoxScope.() -> Unit)
) {
    Scaffold(
        topBar = {
            if (settings.alwaysShowToolbar) {
                CatalogTopAppBar(
                    selectedSample = sample,
                    onBackClick = onBackClick
                )
            }
        },
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding), content = content)
    }
}


@Composable
fun LoadingDialog() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
        Text(
            "Loading...",
            Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        )
    }
}