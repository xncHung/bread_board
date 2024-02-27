package io.xnchung.breadboard.model

import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import java.util.function.Consumer
import java.util.function.Function
import kotlin.reflect.KClass

sealed class Target {
    data class TargetActivity(val targetClass: KClass<out Activity>) : Target()
    data class TargetActivityJava(val targetClass: Class<out Activity>) : Target()

    data class TargetFragment(val targetClass: KClass<out Fragment>) : Target()
    data class TargetFragmentJava(val targetClass: Class<out Fragment>) : Target()

    class TargetFragmentWithBuildFunc<out T : Fragment>(val buildFunc: Map<String, Any?>.() -> T) : Target()
    class TargetFragmentWithJavaBuildFunc<T : Fragment>(val buildFunc: Function<Map<String, Any?>, T>) : Target()

    data class TargetDialog(val targetClass: KClass<out Dialog>) : Target()
    class TargetDialogWithBuildFunc<out T : Dialog>(val buildFunc: Map<String, Any?>.() -> T) : Target()

    data class TargetDialogJava(val targetClass: Class<out Dialog>) : Target()
    class TargetDialogWithJavaBuildFunc<T : Dialog>(val buildFunc: Function<Map<String, Any?>, T>) : Target()

    data class TargetView(val targetClass: KClass<out View>) : Target()
    class TargetViewWithBuildFunc<out T : View>(val buildFunc: Map<String, Any?>.() -> T) : Target()
    data class TargetViewJava(val targetClass: Class<out View>) : Target()
    class TargetViewWithJavaBuildFunc<T : View>(val buildFunc: Function<Map<String, Any?>, T>) : Target()

    class TargetCommonFunc(val func: Map<String, Any?>.() -> Any?) : Target()
    class TargetCommonJavaFunc(val func: Function<Map<String, Any?>, Any?>) : Target()
    class TargetCommonJavaFuncVoid(val func: Consumer<Map<String, Any?>>) : Target()

    class TargetComposable(val composable: @Composable Map<String, Any?>.() -> Unit) : Target()
    data class TargetComposableJava(val classFqn: String, val composable: String) : Target()
}

inline fun <reified T : Activity> targetActivity() = Target.TargetActivity(T::class)
fun <T : Activity> targetActivity(clz: Class<T>) = Target.TargetActivityJava(clz)

inline fun <reified T : Fragment> targetFragment() = Target.TargetFragment(T::class)
fun <T : Fragment> targetFragment(clz: Class<T>) = Target.TargetFragmentJava(clz)
inline fun <reified T : Fragment> targetFragmentBuilder(noinline buildFunc: Map<String, Any?>.() -> T) = Target.TargetFragmentWithBuildFunc(buildFunc)
fun <T : Fragment> targetFragmentBuilderJava(buildFunc: Function<Map<String, Any?>, T>) = Target.TargetFragmentWithJavaBuildFunc(buildFunc)


inline fun <reified T : Dialog> targetDialog() = Target.TargetDialog(T::class)
fun <T : Dialog> targetDialog(clz: Class<T>) = Target.TargetDialogJava(clz)
inline fun <reified T : Dialog> targetDialogBuilder(noinline buildFunc: Map<String, Any?>.() -> T) = Target.TargetDialogWithBuildFunc(buildFunc)
fun <T : Dialog> targetDialogBuilderJava(buildFunc: Function<Map<String, Any?>, T>) = Target.TargetDialogWithJavaBuildFunc(buildFunc)


inline fun <reified T : View> targetView() = Target.TargetView(T::class)
fun <T : View> targetView(clz: Class<T>) = Target.TargetViewJava(clz)
inline fun <reified T : View> targetViewBuilder(noinline buildFunc: Map<String, Any?>.() -> T) = Target.TargetViewWithBuildFunc(buildFunc)
fun <T : View> targetViewBuilderJava(buildFunc: Function<Map<String, Any?>, T>) = Target.TargetViewWithJavaBuildFunc(buildFunc)

fun targetCommonFunc(func: Map<String, Any?>.() -> Any?) = Target.TargetCommonFunc(func)
fun targetCommonFuncJava(func: Function<Map<String, Any?>, Any?>) = Target.TargetCommonJavaFunc(func)
fun targetCommonFuncJavaVoid(func: Consumer<Map<String, Any?>>) = Target.TargetCommonJavaFuncVoid(func)

fun targetComposable(block: @Composable Map<String, Any?>.() -> Unit) = Target.TargetComposable(block)
fun targetComposableJava(classFqn: String, composeFuncFqn: String) = Target.TargetComposableJava(classFqn, composeFuncFqn)
