package io.xnchung.breadboard.converters

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import io.xnchung.breadboard.annotations.DefaultQualifier
import io.xnchung.breadboard.model.ParamHolderContract
import io.xnchung.breadboard.model.TypeToken
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal object ConverterCore {
    private const val TAG = "ConverterCore"
    private val convertersFromDi = mutableListOf<ConverterHolder>()

    /**
     * it will be filled by gradle plugin or hilt
     */
    internal val allExtConverterFactories: Map<String, List<ConverterHolder>> by lazy {
        mutableMapOf<String, MutableList<ConverterHolder>>().apply {
            fillConverterFactories(this)
            convertersFromDi.forEach {
                newConverterHolder(this, it)
            }
        }.mapValues { it.value.sortedBy { it.order } }
    }

    private fun fillConverterFactories(mutableMap: MutableMap<String, MutableList<ConverterHolder>>) {
    }

    internal fun fillDiConverters(converters: Collection<ConverterHolder>) {
        convertersFromDi.addAll(converters)
    }

    @Suppress("unused")
    private fun newConverterHolder(factories: MutableMap<String, MutableList<ConverterHolder>>, holder: ConverterHolder) {
        Log.d(TAG, "newConverterHolder() called with: factories = $factories, holder = $holder")
        factories.getOrPut(holder.qualifier) { mutableListOf() }.add(holder)
    }

    private val builtInConverterFactories = run {
        val qualifiedName = DefaultQualifier::class.qualifiedName!!
        mapOf(qualifiedName to listOf(provideBuiltinConverterFactory<Any>().let { ConverterHolder.Normal(qualifiedName, 0, it) }))
    }

    internal suspend fun <T> convertParam(holder: ParamHolderContract<T>, input: String): T {
        val qualifier = holder.qualifier
        val result = (convertWithConverterFactories(allExtConverterFactories, qualifier, input, holder)
            ?: convertWithConverterFactories(builtInConverterFactories, qualifier, input, holder))
        return requireNotNull(result) {
            "can't convert the input string,input='$input',paramInfo=$holder"
        } as T
    }

    private suspend fun <T> convertWithConverterFactories(
        converterFactories: Map<String, List<ConverterHolder>>,
        qualifier: String,
        input: String,
        holder: ParamHolderContract<T>
    ): Any? {
        val type = holder.getTypeParameterForParamHolder()
        val rawType = rawType(type)
        return if (rawType.isArray || rawType == List::class.java || rawType == ArrayList::class.java) {
            val array = (JsonParser.parseString(input) as? JsonArray).let { requireNotNull(it) { "invalid json array string" } }
            array.mapTo(ArrayList(array.size())) {
                converterFactories[qualifier]?.firstNotNullOfOrNull {
                    when (it) {
                        is ConverterHolder.Normal -> it.func.invoke(it.toString(), type)
                        is ConverterHolder.Suspend -> it.func.invoke(it.toString(), type)
                    }

                }
            }.let {
                if (rawType.isArray) {
                    it.toTypedArray()
                } else {
                    it
                }
            }
        } else {
            converterFactories[qualifier]?.firstNotNullOfOrNull {
                when (it) {
                    is ConverterHolder.Normal -> it.func.invoke(it.toString(), type)
                    is ConverterHolder.Suspend -> it.func.invoke(it.toString(), type)
                }
            }
        }
    }

    private fun rawType(type: Type): Class<*> {
        return when (type) {
            is Class<*> -> {
                type
            }

            is ParameterizedType -> {
                type.rawType as Class<*>
            }

            is GenericArrayType -> {
                val componentType = type.genericComponentType
                java.lang.reflect.Array.newInstance(rawType(componentType), 0).javaClass
            }

            else -> {
                throw IllegalArgumentException("not supported type:$type")
            }
        }
    }

    private fun ParamHolderContract<*>.getTypeParameterForParamHolder(): Type {
        return (this::class.java.genericInterfaces.first { it == TypeToken::class.java } as ParameterizedType).actualTypeArguments.first()
    }
}