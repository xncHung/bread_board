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
    class TargetFragmentWithBuildFunc<out T : Fragment>(val buildFunc: () -> T) : Target()

    data class TargetDialog(val targetClass: KClass<out Dialog>) : Target()
    class TargetDialogWithBuildFunc<out T : Dialog>(val buildFunc: () -> T) : Target()

    data class TargetView(val targetClass: KClass<out View>) : Target()
    class TargetViewWithBuildFunc<out T : View>(val buildFunc: () -> T) : Target()

    class TargetCommonFunc(val func: () -> Unit) : Target()

    class TargetComposable(val composable: @Composable () -> Unit) : Target()
}

inline fun <reified T : Activity> targetActivity() = Target.TargetActivity(T::class)
inline fun <reified T : Fragment> targetFragment() = Target.TargetFragment(T::class)
inline fun <reified T : Fragment> targetFragmentBuilder(noinline buildFunc: () -> T) = Target.TargetFragmentWithBuildFunc(buildFunc)
inline fun <reified T : Dialog> targetDialog() = Target.TargetDialog(T::class)
inline fun <reified T : Dialog> targetDialogBuilder(noinline buildFunc: () -> T) = Target.TargetDialogWithBuildFunc(buildFunc)
inline fun <reified T : View> targetView() = Target.TargetView(T::class)
inline fun <reified T : View> targetViewBuilder(noinline buildFunc: () -> T) = Target.TargetViewWithBuildFunc(buildFunc)
fun targetComposable(block: @Composable () -> Unit) = Target.TargetComposable(block)
