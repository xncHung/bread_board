package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import io.xnchung.breadboard.annotations.Component

/**
 */
@OptIn(KspExperimental::class)
class ComponentProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
    private val isInstrumentation: Boolean,
    val isApp: Boolean
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isApp && !isInstrumentation) {
            generateDiActivity(resolver, codeGenerator)
            generateEmptyDi(resolver, codeGenerator, ClassNames.SAMPLE_CONTRACT_FQN)
        }
        val (valid, invalid) = resolver.getSymbolsWithAnnotation(Component::class.java.name)
            .partition {
                val isValid = it.validate()
                if (!isValid) {
                    logger.warn("this component is not valid ${it.containingFile?.filePath}")
                }
                (it is KSFunctionDeclaration || it is KSClassDeclaration) &&
                        isValid &&
                        !it.isAnnotationPresent(Deprecated::class)
            }
        valid.apply {
            val visitor = ComponentVisitor(logger, resolver, codeGenerator, isInstrumentation, options)
            forEach {
                it.accept(visitor, Unit)
            }
        }
        return invalid
    }
}

internal fun generateEmptyDi(resolver: Resolver, codeGenerator: CodeGenerator, fqn: String) {
    val ksName = resolver.getKSNameFromString(fqn)
    val name = fqn.replace(".", "_").let { "${it}_Module" }
    if (resolver.getClassDeclarationByName("${ClassNames.DI_EMPTY_PACKAGE_NAME}.${name}") != null) {
        return
    }
    if (resolver.getClassDeclarationByName(fqn) == null) {
        return
    }
    val file = codeGenerator.createNewFile(
        dependencies = Dependencies(
            aggregating = false,
        ),
        packageName = ClassNames.DI_EMPTY_PACKAGE_NAME,
        fileName = "_Empty_DI_for_${name}"
    )
    file.use { stream ->
        val clzDeclaration =
            """
               package ${ClassNames.DI_EMPTY_PACKAGE_NAME}
                
               import dagger.Module
               import dagger.Provides
               import dagger.hilt.InstallIn
               import dagger.hilt.components.SingletonComponent
               import dagger.multibindings.ElementsIntoSet
               import $fqn
                
               @Module
               @InstallIn(SingletonComponent::class)
               object $name {
                   @Provides
                   @ElementsIntoSet
                   fun provideEmpty(): Set<${ksName.getShortName()}> = emptySet()
               }
               """.trimIndent()
        stream.write(clzDeclaration.toByteArray())
    }
}

internal fun generateDiActivity(resolver: Resolver, codeGenerator: CodeGenerator) {
    val ksName = resolver.getKSNameFromString(ClassNames.DI_ACTIVITY_FQN)
    if (resolver.getClassDeclarationByName(ksName) != null) {
        return
    }
    val className = ksName.getShortName()
    val packageName = ksName.getQualifier().substringBefore(".$className")
    val file = codeGenerator.createNewFile(
        dependencies = Dependencies(
            aggregating = false,
        ),
        packageName = packageName,
        fileName = className
    )
    file.use { stream ->
        val clzDeclaration =
            """
                package $packageName

                import androidx.activity.viewModels
                import androidx.lifecycle.SavedStateHandle
                import dagger.hilt.android.AndroidEntryPoint
                import dagger.hilt.android.lifecycle.HiltViewModel
                import io.xnchung.breadboard.converters.ConverterHolder
                import io.xnchung.breadboard.launcher.ui.LauncherViewModel
                import io.xnchung.breadboard.model.SampleContract
                import javax.inject.Inject

                @AndroidEntryPoint
                class $className : BaseBreadBoardLauncherActivity() {

                    @HiltViewModel
                    class MyLauncherViewModel @Inject constructor(
                        stateHandle: SavedStateHandle,
                        injectSamples: Set<@JvmSuppressWildcards SampleContract>,
                        injectConverters: Set<@JvmSuppressWildcards ConverterHolder>,
                    ) : LauncherViewModel(injectConverters,stateHandle,injectSamples.toList())

                    override val viewModel: LauncherViewModel by viewModels<MyLauncherViewModel>()
                }

                
                """.trimIndent()
        stream.write(clzDeclaration.toByteArray())
    }
}

