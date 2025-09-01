package com.radarqr.dating.android.ui.welcome.registerScreens

import android.net.Uri
import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.collections.LinkedHashMap


class ImageUploadViewModel : ViewModel() {

    val imageUrlList = LinkedHashMap<Int, String>()
    var imageUriList = LinkedHashMap<Int, Uri>()
    var pair: ArrayList<Pair<String, String>> = ArrayList()
    var clickedPosition = -1
    var size = 6
    fun clearEverything() {
        imageUrlList.clear()
        imageUriList.clear()
        pair.clear()
        size = 6
        clickedPosition = 0
    }
}