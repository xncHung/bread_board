package io.xnchung.breadboard.sample

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommonViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        private const val TAG = "CommonViewModel"
    }

    init {
        savedStateHandle.keys().forEach {
            val any = savedStateHandle.get<Any>(it)
            Log.d(TAG, "arg -> name = $it ,value = $any,class = ${any?.let { it::class }}")
        }
    }
}