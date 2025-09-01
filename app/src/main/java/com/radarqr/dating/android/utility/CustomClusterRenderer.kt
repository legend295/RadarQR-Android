package com.radarqr.dating.android.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.radarqr.dating.android.R
import com.radarqr.dating.android.data.model.Venue
import com.radarqr.dating.android.databinding.LayoutHotspotsClusterMarkerBinding
import com.radarqr.dating.android.databinding.LayoutHotspotsMarkerIconBinding
import com.radarqr.dating.android.utility.mapanimation.MapRipple


class CustomClusterRenderer(
    val context: Context?,
    val map: GoogleMap?,
    clusterManager: ClusterManager<Venue>?,
    val location: Location?,
    val callBack: (MapRipple?, View?) -> Unit
) : DefaultClusterRenderer<Venue>(context, map, clusterManager) {

    var itemId: String? = null
    private val clusterIconGenerator = IconGenerator(context)
    private val markerIconGenerator = IconGenerator(context)
    private var view: LayoutHotspotsClusterMarkerBinding? = null
    private var markerView: LayoutHotspotsMarkerIconBinding? = null
    var icon: Bitmap? = null
    private var markerIcon: Bitmap? = null
    private var mapRipple: MapRipple? = null

    var size: Int = 0


    /**
     * Change cluster color as you want
     * */
    override fun getColor(clusterSize: Int): Int {
        return context?.let { ContextCompat.getColor(it, R.color.teal_200) }
            ?: Color.parseColor("#00BBA0")
    }


    /**
     * If cluster size is less than this size, display individual markers.
     * */
    override fun getMinClusterSize(): Int {
        return 3
    }

    /**
     * By default '+' is added in last of text so we don't need that '+' icon. Hence, removing here
     * and returning only bucket size
     * */
    override fun getClusterText(bucket: Int): String {
        return bucket.toString()
    }

    override fun getDescriptorForCluster(cluster: Cluster<Venue>): BitmapDescriptor {
        clusterIconGenerator.setBackground(
            ContextCompat.getDrawable(
                context!!,
                R.drawable.transparent
            )
        )


        if (view == null || icon == null) {
            Log.d("CUSTOM", "called")
            initializeLayout()
        }

        view?.tvCount?.text = cluster.size.toString()
        callBack(mapRipple, view?.root)

        return BitmapDescriptorFactory.fromBitmap(clusterIconGenerator.makeIcon())

    }

    override fun getBucket(cluster: Cluster<Venue>): Int {
        size = cluster.size
        return super.getBucket(cluster)
    }

    override fun onClusterRendered(cluster: Cluster<Venue>, marker: Marker) {
        super.onClusterRendered(cluster, marker)
        Log.d("CLUSTER", "onClusterRendered")
    }

    override fun onClustersChanged(clusters: MutableSet<out Cluster<Venue>>?) {
        super.onClustersChanged(clusters)
        Log.d("CLUSTER", "onClustersChanged")
    }

    override fun onClusterUpdated(cluster: Cluster<Venue>, marker: Marker) {
        super.onClusterUpdated(cluster, marker)
        Log.d("CLUSTER", "onClusterUpdated")
    }

    override fun onClusterItemUpdated(item: Venue, marker: Marker) {
        marker.setIcon(getBitmap(item.roamingTimerActiveUsersCount))
        super.onClusterItemUpdated(item, marker)
        Log.d("CLUSTER", "onClusterItemUpdated")
    }

    override fun onClusterItemRendered(clusterItem: Venue, marker: Marker) {
        marker.setIcon(getBitmap(clusterItem.roamingTimerActiveUsersCount))
        super.onClusterItemRendered(clusterItem, marker)
        Log.d("CLUSTER", "onClusterItemUpdated")
    }

    override fun onBeforeClusterItemRendered(item: Venue, markerOptions: MarkerOptions) {
        markerOptions.icon(getBitmap(item.roamingTimerActiveUsersCount))
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    private fun initializeMarkerLayout() {
        markerView =
            LayoutHotspotsMarkerIconBinding.inflate(LayoutInflater.from(context), null, false)
        markerIconGenerator.setContentView(markerView?.root)

        markerIcon = markerIconGenerator.makeIcon()
    }

    private fun getBitmap(count: Int): BitmapDescriptor {
        markerIconGenerator.setBackground(
            ContextCompat.getDrawable(
                context!!,
                R.drawable.transparent
            )
        )

        if (markerView == null || markerIcon == null) {
            initializeMarkerLayout()
        }
        markerView?.tvCount?.text = "$count"
        return BitmapDescriptorFactory.fromBitmap(markerIconGenerator.makeIcon())
    }


    private fun initializeLayout() {
        view =
            LayoutHotspotsClusterMarkerBinding.inflate(LayoutInflater.from(context), null, false)
        clusterIconGenerator.setContentView(view?.root)
        icon = clusterIconGenerator.makeIcon()
    }
}