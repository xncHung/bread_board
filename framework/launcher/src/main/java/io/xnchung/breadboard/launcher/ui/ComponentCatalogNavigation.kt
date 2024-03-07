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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.xnchung.breadboard.launcher.Constants
import io.xnchung.breadboard.model.SampleContract
import io.xnchung.breadboard.model.Target

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
            CatalogScreen(samples, settings) {
                navController.navigate(route)
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
    when (val target = sample.target) {
        is Target.TargetComposable -> {
            composable(sample.route) {
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
            composable(sample.route) {
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
            activity(sample.route) {
                label = sample.name
                activityClass = target.targetClass
            }
        }

        is Target.TargetActivityJava -> {
            activity(sample.route) {
                label = sample.name
                activityClass = target.targetClass.kotlin
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
