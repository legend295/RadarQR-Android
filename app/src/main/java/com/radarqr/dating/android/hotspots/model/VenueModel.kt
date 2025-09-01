package com.radarqr.dating.android.hotspots.model

import com.google.gson.annotations.SerializedName
import com.radarqr.dating.android.data.model.UpdateVenueImages
import com.radarqr.dating.android.data.model.VenueImage
import java.io.Serializable

data class CreateVenue(var name: String, var uniquename: String)

data class CreateVenueResponse(
    val venue_id: String
)

data class MyVenuesData(
    val __v: Int,
    val _id: String = "",
    val ambiance: Ambiance?,
    val completepercentage: Int = 0,
    val contactinfo: ContactInfo,
    val createdAt: String,
    val description: String?,
    val extrainfo: String?,
    val images: ArrayList<VenueImage>?,
    val name: String,
    val specialoffer: String?,
    val status: Int,
    val type: Type?,
    val uniquename: String,
    val updatedAt: String,
    val user_id: String,
    var isReadyForApproval: Boolean = false,
    val roamingtimeractiveuserscount: Int = 0,
    @SerializedName("pause_comment")
    val pauseComment: String? = null
) : Serializable


data class Type(val name: String?, val value: String?) : Serializable
data class Ambiance(val name: String?, val value: String?) : Serializable

data class VenueResponse(
    val venues: ArrayList<MyVenuesData>?,
    val total_count: Int

) : Serializable

data class UpdateVenueRequest(
    var ambiance: Ambiance? = null,
    var name: String? = null,
    var uniquename: String? = null,
    var contactinfo: ContactInfo? = null,
    var description: String? = null,
    var extrainfo: String? = null,
    var specialoffer: String? = null,
    var type: Type? = null,
    var image: UpdateVenueImages? = null,
    var venue_id: String? = null
) : Serializable {
    fun setData(data: MyVenuesData) {
        ambiance = data.ambiance
        name = data.name
        description = data.description
        extrainfo = data.extrainfo
        specialoffer = data.specialoffer
        type = data.type
        venue_id = data._id
        contactinfo = ContactInfo(
            id = data.contactinfo.id,
            name = data.contactinfo.name,
            address = data.contactinfo.address,
            city = data.contactinfo.city,
            country = data.contactinfo.country,
            locality = data.contactinfo.locality,
            state = data.contactinfo.state,
            phonenumber = data.contactinfo.phonenumber,
            website = data.contactinfo.website,
            latlon = data.contactinfo.latlon
        )
        if ((data.contactinfo.latlon?.coordinates?.size ?: 0) >= 2) {
            contactinfo?.lat = data.contactinfo.latlon?.coordinates?.get(1)
            contactinfo?.lng = data.contactinfo.latlon?.coordinates?.get(0)
        }
    }
}

data class ContactInfo(
    var city: String? = null,
    var address: String? = null,
    var name: String? = null,
    var id: String? = null,
    var country: String? = null,
    var lat: Double? = null,
    var lng: Double? = null,
    var locality: String? = null,
    var phonenumber: String? = null,
    var state: String? = null,
    var website: String? = null,
    var latlon: Latlon? = null
) : Serializable

data class Latlon(
    var coordinates: ArrayList<Double>? = null,
    val type: String = "Point"
) : Serializable

data class DeleteVenue(val venue_id: String)

data class VenueTypeAndAmbianceResponse(
    val name: String,
    val value: String,
    var isSelected: Boolean = false
)

data class SubmitVenue(val venue_id: String)
