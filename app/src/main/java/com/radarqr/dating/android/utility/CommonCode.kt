package com.radarqr.dating.android.utility

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.radarqr.dating.android.R


class CommonCode {

    companion object {

        fun setToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//            Toast(context).also {
//                // View and duration has to be setP
//                val view1 = LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
//                it.view = view1
//                it.duration = Toast.LENGTH_SHORT
//              // it.setText(message)
//                view1.toast_text.text = message
//                it.show()
//            }
        }
    }
}