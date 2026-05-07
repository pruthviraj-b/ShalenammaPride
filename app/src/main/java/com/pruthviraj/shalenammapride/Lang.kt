package com.pruthviraj.shalenammapride

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Lang {
    var isKannada by mutableStateOf(false)

    fun toggle() {
        isKannada = !isKannada
    }

    // A helper function that returns the Kannada string if toggled, otherwise English.
    fun get(en: String, kn: String): String {
        return if (isKannada) kn else en
    }
}
