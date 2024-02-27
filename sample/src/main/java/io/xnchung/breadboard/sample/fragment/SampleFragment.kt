package io.xnchung.breadboard.sample.fragment

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.xnchung.breadboard.annotations.Component
import io.xnchung.breadboard.annotations.ConverterProvider
import io.xnchung.breadboard.annotations.Param
import io.xnchung.breadboard.annotations.ParamProvider
import io.xnchung.breadboard.annotations.ParamProviderContract
import io.xnchung.breadboard.sample.R
import kotlinx.parcelize.Parcelize
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Component(
    name = "testFragment",
    params = [
        Param(key = "stringParam", rawType = String::class, nullable = true, provider = StringProvider::class),
        Param(key = "intParam", rawType = Int::class),
        Param(
            key = "parcelParam",
            rawType = SampleFragment.ParcelParam::class
        )]
)
class SampleFragment : Fragment() {
    @Parcelize
    data class ParcelParam(
        val string: String,
        val int: Int
    ) : Parcelable

    private var mViewModel: SampleViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val argsMap = arguments?.run {
            keySet().associateWith { get(it) }
        }
        Log.d(TAG, "onCreateView: args=$argsMap")
        return inflater.inflate(R.layout.fragment_sample, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(SampleViewModel::class.java)
        // TODO: Use the ViewModel
    }

    companion object Test {
        @Component(name = "fragment newInstance method")
        @JvmStatic
        fun newInstance(
            context: Context,
            string: String,
            intParam: Int,
            parcelParam: ParcelParam
        ): SampleFragment {
            return SampleFragment().apply {
                arguments = Bundle().apply {
                    putString("stringParam", string)
                    putInt("intParam", intParam)
                    putParcelable("parcelParam", parcelParam)
                }
            }
        }

        @ConverterProvider
        fun provideDateConverter(): String.(Type) -> Date? = l@{
            try {
                if (it != Date::class.java) return@l null
                SimpleDateFormat("yyyyMMdd", Locale.US).parse(this)
            } catch (e: Exception) {
                null
            }
        }

        private const val TAG = "SampleFragment"
    }
}

object StringProvider : ParamProviderContract<String> {
    override val values: Iterable<String>
        get() = listOf("Test1", "Test2")
}

object TestKtObject {
    @ConverterProvider
    fun provideDateConverter(): String.(Type) -> Date? = l@{
        try {
            if (it != Date::class.java) return@l null
            SimpleDateFormat("yyyyMMdd", Locale.US).parse(this)
        } catch (e: Exception) {
            null
        }
    }


    @Component("unit func")
    fun funcComponent(@ParamProvider(StringProvider::class) string: String) {
        println("io.xnchung.breadboard.sample.fragment.TestKtObject.funcComponent called with param:$string")
    }

    @Component("unit func")
    fun funcComponent1(string: String, int: Int): String {
        println("io.xnchung.breadboard.sample.fragment.TestKtObject.funcComponent1 called with param:$string,$int")
        return "Hello"
    }
}
