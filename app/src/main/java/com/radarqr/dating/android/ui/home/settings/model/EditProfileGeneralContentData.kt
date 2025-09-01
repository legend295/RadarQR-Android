package com.radarqr.dating.android.ui.home.settings.model

import com.radarqr.dating.android.utility.EditProfileGeneralContentTypes

data class EditProfileGeneralContentData(
    var id: Int,
    var contentType: EditProfileGeneralContentTypes,
    var title: String,
    var value: String,
    var originalValue: String,
    var nonNegotiableKey: Boolean = false
)
