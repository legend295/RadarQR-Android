package com.quickblox.sample.chat.kotlin.utils

import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.radarqr.dating.android.app.RaddarApp

@IntDef(Toast.LENGTH_LONG, Toast.LENGTH_SHORT)
private annotation class ToastLength

fun shortToast(@StringRes text: Int) {
    shortToast(RaddarApp.getInstance().getString(text))
}

fun shortToast(text: String?) {
    show(text, Toast.LENGTH_SHORT)
}

fun longToast(@StringRes text: Int) {
    longToast(RaddarApp.getInstance().getString(text))
}

fun longToast(text: String) {
    show(text, Toast.LENGTH_LONG)
}

private fun show(text: String?, @ToastLength length: Int) {
    text?.let {
        makeToast(it, length).show()
    }
}

private fun makeToast(text: String, @ToastLength length: Int): Toast {
    return Toast.makeText(RaddarApp.getInstance(), text, length)
}