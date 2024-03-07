package io.xnchung.breadboard.launcher.ui

import android.os.Parcelable
import io.xnchung.breadboard.model.SampleContract
import io.xnchung.breadboard.model.Target
import kotlinx.parcelize.Parcelize

internal data class CatalogSettings(
    val alwaysShowToolbar: Boolean = true,
    val filters: List<CatalogFilterFactory> = listOf(CatalogFilterFactory.Module, CatalogFilterFactory.Category, CatalogFilterFactory.Tag),
    val cardAppearance: CatalogCardAppearance = CatalogCardAppearance()
)

internal data class CatalogCardAppearance(
    val description: Int = Int.MAX_VALUE,
    val tags: Boolean = true,
    val owners: Boolean = true
)

internal sealed class CatalogFilterFactory {
    abstract fun fromSamples(sampleContract: List<SampleContract>): List<CatalogFilter>

    object Module : CatalogFilterFactory() {
        override fun fromSamples(sampleContract: List<SampleContract>): List<CatalogFilter> {
            return sampleContract.groupBy {
                it.moduleName
            }.keys.sorted().map {
                CatalogFilter.Module(it)
            }
        }

    }

    object Category : CatalogFilterFactory() {
        override fun fromSamples(sampleContract: List<SampleContract>): List<CatalogFilter> {
            return sampleContract.groupBy {
                it.sampleCategory()
            }.keys.sorted().map { CatalogFilter.Category(it) }
        }


    }

    object Tag : CatalogFilterFactory() {
        override fun fromSamples(sampleContract: List<SampleContract>): List<CatalogFilter> {
            return sampleContract.flatMap { it.tags }.distinct().sorted().map {
                CatalogFilter.Tag(it)
            }
        }
    }

}

private fun SampleContract.sampleCategory() = when (target) {
    is Target.TargetActivity,
    is Target.TargetActivityJava -> {
        SampleCategory.Activity
    }

    is Target.TargetCommonFunc,
    is Target.TargetCommonJavaFunc,
    is Target.TargetCommonJavaFuncVoid -> {
        SampleCategory.Func
    }

    is Target.TargetComposable,
    is Target.TargetComposableJava -> {
        SampleCategory.Composable
    }

    is Target.TargetDialog,
    is Target.TargetDialogJava,
    is Target.TargetDialogWithBuildFunc<*>,
    is Target.TargetDialogWithJavaBuildFunc<*> -> {
        SampleCategory.Dialog
    }

    is Target.TargetFragment,
    is Target.TargetFragmentJava,
    is Target.TargetFragmentWithBuildFunc<*>,
    is Target.TargetFragmentWithJavaBuildFunc<*> -> {
        SampleCategory.Fragment
    }

    is Target.TargetView,
    is Target.TargetViewJava,
    is Target.TargetViewWithBuildFunc<*>,
    is Target.TargetViewWithJavaBuildFunc<*> -> {
        SampleCategory.View
    }
}

internal enum class SampleCategory {
    Activity,
    Fragment,
    Composable,
    Dialog,
    View,
    Func,
}

internal sealed interface CatalogFilter : Parcelable {
    fun accept(sampleContract: SampleContract): Boolean
    fun displayName(): String

    @Parcelize
    data class Tag(private val tag: String) : CatalogFilter {
        override fun accept(sampleContract: SampleContract): Boolean = sampleContract.tags.any { it.equals(tag, true) }
        override fun displayName(): String {
            return tag
        }
    }

    @Parcelize
    data class Module(private val simpleName: String) : CatalogFilter {
        override fun accept(sampleContract: SampleContract): Boolean {
//            java 目前不能自动获取 path
            return sampleContract.moduleName == simpleName
        }

        override fun displayName(): String {
            return simpleName
        }
    }

    @Parcelize
    data class Category(private val category: SampleCategory) : CatalogFilter {
        override fun accept(sampleContract: SampleContract): Boolean {
            return sampleContract.sampleCategory() == category
        }

        override fun displayName(): String {
            return category.name
        }
    }
}
