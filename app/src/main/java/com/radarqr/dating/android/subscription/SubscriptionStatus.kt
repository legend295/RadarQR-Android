package com.radarqr.dating.android.subscription

enum class SubscriptionStatus : ISubscriptionStatus {
    PLUS {
        override fun getSwipes(): Int = Int.MAX_VALUE

        override fun canViewLikes(): Boolean = true
        override fun canSetPreferencesNonNegotiable(): Boolean= true
    },
    NON_PLUS {

        override fun getSwipes(): Int = 5

        override fun canViewLikes(): Boolean = false
        override fun canSetPreferencesNonNegotiable(): Boolean= false
    }
}