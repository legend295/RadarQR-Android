package com.radarqr.dating.android.subscription

interface ISubscriptionStatus {

    fun getSwipes(): Int
    fun canViewLikes(): Boolean

    fun canSetPreferencesNonNegotiable(): Boolean
}