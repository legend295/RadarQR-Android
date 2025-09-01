package com.radarqr.dating.android.utility

import com.amazonaws.regions.Regions
import com.radarqr.dating.android.BuildConfig


object AWSKeys {
    //    internal const val COGNITO_POOL_ID = "us-west-2:fa9f5900-624d-4c4e-be26-93f38806d3a5"
    internal const val COGNITO_POOL_ID = "us-west-2:d7459fb4-fe0f-4e56-b397-8c138f8cb4dc"
    internal val MY_REGION = Regions.US_WEST_2 // WHAT EVER REGION IT MAY BE, PLEASE CHOOSE EXACT
    const val BUCKET_NAME = BuildConfig.BUCKET_NAME
    const val BUCKET_NAME_MEDIUM = BuildConfig.BUCKET_NAME_MEDIUM
    const val BUCKET_NAME_THUMB = BuildConfig.BUCKET_NAME_THUMB
    const val BUCKET_NAME_VENUE = BuildConfig.BUCKET_NAME_VENUE
    const val BUCKET_NAME_VENUE_MEDIUM = BuildConfig.BUCKET_NAME_VENUE_MEDIUM
    const val BUCKET_NAME_VENUE_THUMB = BuildConfig.BUCKET_NAME_VENUE_THUMB
}