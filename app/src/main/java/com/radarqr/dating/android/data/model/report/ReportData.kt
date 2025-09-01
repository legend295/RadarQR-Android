package com.radarqr.dating.android.data.model.report

data class ReportData(
    val `data`: ArrayList<Data>,
    val message: String,
    val type: String,
    val status: Int
)

data class Data(
    val _id: String = "",
    val option: String = "",
    val type: String = "",
    val sub_options: ArrayList<SubOption> = ArrayList()
)

data class SubOption(
    val _id: String = "",
    val option_id: String = "",
    val type: String = "",
    val sub_suboptions: ArrayList<SubSuboption> = ArrayList(),
    val value: String = ""
)

data class SubSuboption(
    val _id: String = "",
    val sub_option_id: String = "",
    val type: String = "",
    val value: String = ""
)