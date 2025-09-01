package com.radarqr.dating.android.ui.home.settings.model

class CommonModel(val name: String, val type: String, val sent_value: String? = "") {
    var isSelected = false
    @JvmName("setSelected1")
    fun setSelected(selected: Boolean) {
        isSelected = selected
    }


    @JvmName("isSelected1")
    fun isSelected(): Boolean {
        return isSelected
    }

}