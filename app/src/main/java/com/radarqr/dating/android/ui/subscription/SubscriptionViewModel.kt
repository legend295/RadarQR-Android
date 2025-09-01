package com.radarqr.dating.android.ui.subscription

import android.app.Application
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.radarqr.dating.android.R
import com.radarqr.dating.android.ui.subscription.model.StoreProducts
import com.revenuecat.purchases.models.StoreProduct
import org.json.JSONObject
import kotlin.math.ceil
import kotlin.math.round

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    val isSeeAllClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val featuresList: ArrayList<SubscriptionVerticalFeaturesData> = ArrayList()
    val featuresListHorizontal: ArrayList<SubscriptionHorizontalFeaturesData> = ArrayList()
    val productsList = ArrayList<StoreProducts>()
    val revenueCatProducts = ArrayList<StoreProduct>()
    private val pricePerDuration = JSONObject()
    private var currency: String = "$"

    init {
        setFeaturesList()
        setFeaturesListHorizontal()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSeeAllClick(v: View) {
        isSeeAllClicked.value = !(isSeeAllClicked.value ?: false)
    }

    private fun setFeaturesList() {
        featuresList.clear()
        featuresList.apply {
            add(
                SubscriptionVerticalFeaturesData(
                    "See Who Likes You",
                    getDrawable(R.drawable.ic_black_like_subscription)
                )
            )
            add(
                SubscriptionVerticalFeaturesData(
                    "Contact Singles at Hot Spots",
                    getDrawable(R.drawable.ic_black_hotspot_subscription)
                )
            )
            add(
                SubscriptionVerticalFeaturesData(
                    "Unlimited Swipes",
                    getDrawable(R.drawable.ic_black_unlimited_swipes_subscription)
                )
            )
            add(
                SubscriptionVerticalFeaturesData(
                    "Remove Ads",
                    getDrawable(R.drawable.ic_black_no_ad_subscription)
                )
            )
            add(
                SubscriptionVerticalFeaturesData(
                    "Comment Before Matching",
                    getDrawable(R.drawable.ic_black_comment_before_match_subscription)
                )
            )
            add(
                SubscriptionVerticalFeaturesData(
                    "Recall (go back to last profile)",
                    getDrawable(R.drawable.ic_black_recall_subscription)
                )
            )
            add(
                SubscriptionVerticalFeaturesData(
                    "Discover Tagged Singles at Venues",
                    getDrawable(R.drawable.ic_black_tagged_subscription)
                )
            )
        }
    }

    private fun setFeaturesListHorizontal() {
        featuresListHorizontal.clear()
        featuresListHorizontal.apply {
            add(
                SubscriptionHorizontalFeaturesData(
                    "See Who Likes You",
                    "Save time and view who liked\nyou first!",
                    getDrawable(R.drawable.ic_horizontal_subscription)
                )
            )
            add(
                SubscriptionHorizontalFeaturesData(
                    "Contact Singles at Hot Spots",
                    "Introduce yourself in a flash! Instantly connect with other singles at your preferred Hot Spots using this feature.",
                    getDrawable(R.drawable.ic_horizontal_subscription_second)
                )
            )
            add(
                SubscriptionHorizontalFeaturesData(
                    "Comment Before Matching",
                    "Stand out from the crowd\nwith a great ice-breaker that\nshows your personality!",
                    getDrawable(R.drawable.ic_horizontal_subscription_third)
                )
            )
        }
    }

    private fun getDrawable(id: Int): Drawable? {
        return ContextCompat.getDrawable(getApplication(), id)
    }

    fun storePricePerDuration() {
        revenueCatProducts.forEach { product ->
            currency = product.price.formatted.substring(0, 1)
            //Remove currency sign and also replace , from the price and store into price
            val price = product.price.formatted.substring(1).replace(",", "").toDouble()

            //store name as  product value + product unit e.g - 1WEEK and store price as value
            pricePerDuration.put("${product.period?.value}${product.period?.unit}", price)
        }
    }

    fun calculateMoneySavedOverOneWeek(products: List<StoreProduct>) {
        val iterator = products.iterator()

        while (iterator.hasNext()) {
//            calculateSingleProductSavingAmount(iterator.next())
        }

    }

    fun calculateSingleProductSavingAmount(product: StoreProduct, products: StoreProducts) {
        // First find out the weekly price as we need to calculate saved price percentage over weekly price
        if (!pricePerDuration.has("1$WEEK")) return
        val weekPrice = pricePerDuration["1$WEEK"].toString().toDouble()

        // Leave Weekly price as we don't need to calculate for this
        if (product.period?.unit.toString() != WEEK) {
            // If Unit is Month
            if (product.period?.unit.toString() == MONTH) {
                // store monthly price
                val monthlyPrice =
                    pricePerDuration["${product.period?.value}$MONTH"].toString().toDouble()
                // calculate total price - week in month * value e.g 4 * quantity of months, quantity of months can be 1, 3, 6
                val totalWeeks = (WEEK_IN_MONTH * (product.period?.value?.toString()?.toInt() ?: 1))
                val totalWeekPrice = weekPrice * totalWeeks
                // find saved price by subtracting totalWeekPrice - Monthly price
                val savedPrice = totalWeekPrice - monthlyPrice
                // Calculate percentage of saving amount
                val savedPricePercentage =
                    (savedPrice / totalWeekPrice) * 100
                Log.d(
                    SubscriptionViewModel::class.simpleName,
                    "Monthly save - ${round(savedPricePercentage)} %"
                )
                products.key = "${product.period?.value}${product.period?.unit}"
                products.savingPercentage = round(savedPricePercentage).toInt()
                products.amountPerWeek =
                    "$currency ${String.format("%.2f", (monthlyPrice / totalWeeks))}"
            } else if (product.period?.unit.toString() == YEAR) {
                // store yearly price
                val yearlyPrice =
                    pricePerDuration["${product.period?.value}$YEAR"].toString().toDouble()
                val totalWeeks = (WEEK_IN_YEAR * (product.period?.value?.toString()?.toInt() ?: 1))
                // calculate total price - weeks in year * value e.g 52 * quantity of years, quantity of years can be 1, 2, 3
                val totalYearlyPrice = weekPrice * totalWeeks
                // find saved price by subtracting totalYearlyPrice - yearlyPrice price
                val savedPrice = totalYearlyPrice - yearlyPrice
                // Calculate percentage of saving amount
                val savedPricePercentage =
                    (savedPrice / totalYearlyPrice) * 100
                Log.d(
                    SubscriptionViewModel::class.simpleName,
                    "Yearly save ${round(savedPricePercentage)}%"
                )
                products.key = "${product.period?.value}${product.period?.unit}"
                products.savingPercentage = round(savedPricePercentage).toInt()
                products.amountPerWeek =
                    "$currency ${String.format("%.2f", (yearlyPrice / totalWeeks))}"
            }
        } else {
            products.key = "${product.period?.value}${product.period?.unit}"
            products.amountPerWeek =
                "$currency ${String.format("%.2f", (weekPrice))}"
        }

        productsList.add(products)
    }

    private fun sortList() {
        val tempList = ArrayList(productsList)

    }

    data class SubscriptionHorizontalFeaturesData(
        var title: String,
        var description: String,
        var icon: Drawable?
    )

    data class SubscriptionVerticalFeaturesData(
        var title: String,
        var icon: Drawable?
    )

    companion object {
        const val WEEK = "WEEK"
        const val MONTH = "MONTH"
        const val YEAR = "YEAR"
        const val WEEK_IN_MONTH = 4
        const val WEEK_IN_YEAR = 52
    }
}