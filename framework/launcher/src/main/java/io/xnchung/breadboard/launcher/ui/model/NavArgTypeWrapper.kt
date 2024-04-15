package io.xnchung.breadboard.launcher.ui.model

import android.os.Bundle
import androidx.navigation.NavType
import io.xnchung.breadboard.converters.ConverterRepo
import io.xnchung.breadboard.model.ParamHolderContract
import io.xnchung.breadboard.model.SampleContract
import java.lang.reflect.Type

class NavArgTypeWrapper(val type: Type, val sample: SampleContract, val holderContract: ParamHolderContract<*>) :
    NavType<Any?>(holderContract.nullable) {
    @Suppress("DEPRECATION")
    override fun get(bundle: Bundle, key: String): Any? {
        return bundle.get(key)
    }

    override fun parseValue(value: String): Any? {
        return ConverterRepo.getAndRemoveArg(sample, holderContract, value)
    }

    override fun put(bundle: Bundle, key: String, value: Any?) {
        bundle.putValue(key to value)
    }
}