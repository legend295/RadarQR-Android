package com.radarqr.dating.android.data.model.hobbies


data class HobbiesAndInterestData(
    val _id: String = "",
    var isSelected: Boolean = false,
    val name: String = "",
    val value: String = "",
    val image: String = "",
    val category: String = "",
)

data class Hobbies(
    val `data`: ArrayList<HobbiesAndInterestData>,
    val message: String,
    val status: Int
)
