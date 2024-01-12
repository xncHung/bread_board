package io.xnchung.breadboard.model

import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

sealed class Target {
    data class TargetActivity(val targetClass: KClass<out Activity>) : Target()
    data class TargetFragment(val targetClass: KClass<out Fragment>) : Target()
    data class TargetDialog(val targetClass: KClass<out Dialog>) : Target()
    data class TargetView(val targetClass: KClass<out View>) : Target()
    class TargetComposable(val composable: @Composable () -> Unit) : Target()
}