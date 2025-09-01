package com.radarqr.dating.android.subscription

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.radarqr.dating.android.BuildConfig
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.utility.Utility.showToast
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.ProductType
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getProductsWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.SyncPurchasesCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import java.util.Date

object SubscriptionWrapper {
    private val tag = SubscriptionWrapper::class.java.simpleName
    var activeSubscriptionId = ""
    var expirationDate: Date? = null

    fun Context.initializeRevenueCatSDK() {
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(this, BuildConfig.RC_API_KEY).build()
        )
        updateCustomerInfoListener()
//        syncPurchases()
    }

    private fun syncPurchases() {
        Purchases.sharedInstance.syncPurchases(object : SyncPurchasesCallback {
            override fun onError(error: PurchasesError) {
                Log.e(
                    tag,
                    "syncPurchases -- error ${error.message}"
                )
            }

            override fun onSuccess(customerInfo: CustomerInfo) {
                Log.d(
                    tag,
                    "syncPurchases -- success ${customerInfo.activeSubscriptions.size}"
                )
                customerInfo.manageSubscription()
            }
        })
    }

    fun String?.loginRCAppUserId(callback: (CustomerInfo) -> Unit) {
        if (this.isNullOrEmpty()) return
        // Later log in provided user Id
        Purchases.sharedInstance.logInWith(this, ::showError) { customerInfo, created ->
//            Purchases.sharedInstance.setDisplayName(profileData.name)
            // customerInfo updated for my_app_user_id
            Log.d(
                tag,
                "user logged-in -- Active subscription size ${customerInfo.activeSubscriptions.size}"
            )
            customerInfo.manageSubscription()
            callback(customerInfo)
        }
    }

    private fun CustomerInfo.manageSubscription() {
        activeSubscriptions.forEach {
            activeSubscriptionId = it
        }
        expirationDate = this.latestExpirationDate
        if (activeSubscriptions.isNotEmpty()) {
            RaddarApp.getInstance().setSubscriptionStatus(SubscriptionStatus.PLUS)
        } else {
            activeSubscriptionId = ""
            RaddarApp.getInstance().setSubscriptionStatus(SubscriptionStatus.NON_PLUS)
        }
    }

    fun restoreSubscription(callback: (CustomerInfo) -> Unit) {
        Purchases.sharedInstance.restorePurchasesWith(onError = { error ->
            Log.e(tag, "Error restoring subscription -- ${error.code}, message -- ${error.message}")
        }, onSuccess = { customerInfo ->
            customerInfo.manageSubscription()
            callback(customerInfo)
            Log.d(
                tag,
                "Subscription restored, active subscription is - ${customerInfo.activeSubscriptions.size}"
            )
        })
    }

    fun logOut() {
//        Purchases.sharedInstance.logOut()
//        isLoggedIn = false
    }

    private fun showError(error: PurchasesError) {
        //handle error during login
        Log.e(tag, "Error user login -- ${error.code}, message -- ${error.message}")
    }

    fun String.setPushToken() {
        Purchases.sharedInstance.setPushToken(this)
    }

    fun getUserInformation(info: (CustomerInfo, Boolean, String) -> Unit) {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                Log.e(tag, "Error user login -- ${error.code}, message -- ${error.message}")
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                Log.d(
                    tag,
                    "getCustomerInfoWith -- Active subscription size ${customerInfo.activeSubscriptions.size}"
                )
                customerInfo.manageSubscription()
                val hasSubscription = customerInfo.activeSubscriptions.isNotEmpty()
                info(customerInfo, hasSubscription, activeSubscriptionId)
            }

        })
    }

    private fun updateCustomerInfoListener() {
        Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener {
            Log.d(
                tag,
                "updateCustomerInfoListener -- success ${it.activeSubscriptions.size}"
            )
        }
    }

    fun fetchAndDisplayAvailableProducts(products: (List<StoreProduct>) -> Unit) {

        val list = arrayListOf(
            "com.radarqr.dating.android.plus"
        )
        /*val list = arrayListOf(
            "com.radarqr.dating.android.pickuplines"
        )*/
        Purchases.sharedInstance.getProductsWith(list,
            onError = {
                Log.e(
                    tag,
                    "Error while fetching products with code -- ${it.code} , with message -- ${it.message}"
                )
            }, onGetStoreProducts = {
                Log.d(tag, "Products received successfully -- ${it.size}")
                products(it)
            })
    }

    fun Activity.makePurchase(
        storeProduct: StoreProduct,
        callback: (StoreTransaction?, CustomerInfo, String) -> Unit,
        onError: (error: PurchasesError, userCancelled: Boolean) -> Unit
    ) {
        Purchases.sharedInstance.purchaseWith(
            PurchaseParams.Builder(this, storeProduct).build(),
            onError = { error, userCancelled ->
                Log.e(
                    tag,
                    "Error while purchasing the product -- ${error.code} , with message -- ${error.message}"
                )
                val msg = if (userCancelled) "Transaction Canceled" else "Transaction failed."
                onError(error, userCancelled)
                showToast(msg)
            },
            onSuccess = { storeTransaction, customerInfo ->
                customerInfo.manageSubscription()
                callback(storeTransaction, customerInfo, activeSubscriptionId)
                showToast("Transaction success.")
            })
    }
}