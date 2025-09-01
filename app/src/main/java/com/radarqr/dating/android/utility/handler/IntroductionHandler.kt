package com.radarqr.dating.android.utility.handler

import com.radarqr.dating.android.utility.introduction.IntroductionScreenType

interface IntroductionHandler {
    fun showIntroductoryUI(
        type: IntroductionScreenType,
        hasShown: Boolean,
        co_ordinates: Pair<Float, Float>,
        callBack: (isRedirecting:Boolean) -> Unit
    )
}