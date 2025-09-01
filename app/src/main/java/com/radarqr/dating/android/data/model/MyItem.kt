package com.radarqr.dating.android.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class Venue(
    val _id: String,
    @SerializedName("contactinfo")
    val contactInfo: ContactInfo,
    val dist: Dist,
    val name: String,
    val status: Int,
    snippet: String,
    @SerializedName("roamingtimeractiveuserscount")
    val roamingTimerActiveUsersCount: Int = 0,
) : ClusterItem {

    private val position: LatLng = LatLng(contactInfo.latLon.coordinates[1], contactInfo.latLon.coordinates[0])
    private val title: String = name
    private val snippet: String
    private var venueId: String = ""
    override fun getPosition(): LatLng {
        return position
    }

    fun getId(): String = venueId

    override fun getTitle(): String {
        return title
    }

    override fun getSnippet(): String {
        return snippet
    }

    init {
        this.snippet = snippet
        this.venueId = _id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Venue) {
            return false
        }
        val that: Venue = other
        return Objects.equals(venueId, that.venueId)
    }

    override fun hashCode(): Int {
        return Objects.hash(venueId)
    }
}