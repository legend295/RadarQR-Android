package com.radarqr.dating.android.ui.home.settings.model

import android.graphics.drawable.Drawable

data class SettingsOptionData(
    val title: String,
    val position: Int,
    var endValue: Int,
    val iconDrawable: Drawable?,
    val type: SettingsTYpe
) {
    enum class SettingsTYpe {
        TIPS_TRICKS, PREFERENCES, ACCOUNT,  VENUES, CLOSE_FRIENDS, LOGOUT
    }
}
