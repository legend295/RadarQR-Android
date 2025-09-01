package com.radarqr.dating.android.ui.subscription

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.BuildConfig
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentSubscriptionBinding
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.subscription.SubscriptionWrapper
import com.radarqr.dating.android.subscription.SubscriptionWrapper.makePurchase
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.subscription.adapter.SubscriptionFeaturesHorizontalAdapter
import com.radarqr.dating.android.ui.subscription.adapter.SubscriptionFeaturesVerticalAdapter
import com.radarqr.dating.android.ui.subscription.adapter.SubscriptionOffersAdapter
import com.radarqr.dating.android.ui.subscription.model.StoreProducts
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.getStringWithFirstWordCapital
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.PreferencesHelper.PreferencesKeys.ADS_COUNT
import com.radarqr.dating.android.utility.Utility.color
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import com.revenuecat.purchases.models.StoreProduct
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubscriptionFragment : BaseFragment<FragmentSubscriptionBinding>(), ViewClickHandler {

    private val subscriptionViewModel: SubscriptionViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()

    private val adapterHorizontalFeatures by lazy {
        SubscriptionFeaturesHorizontalAdapter(
            subscriptionViewModel.featuresListHorizontal
        )
    }
    private val adapterVerticalFeatures by lazy {
        SubscriptionFeaturesVerticalAdapter(
            subscriptionViewModel.featuresList
        )
    }
    private var hasSubscription: Boolean = false
    private val adapterSubscriptionOffer by lazy { SubscriptionOffersAdapter() }
    private var selectedProduct: StoreProducts? = null
    private var activeSubscriptionId = ""
    private var expirationDate: Date? = null
    private var fromScreen: String? = null
    private var adsFrom: String? = null
    override fun getLayoutRes(): Int = R.layout.fragment_subscription

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
        fromScreen = arguments?.getString(Constants.FROM)
        adsFrom = arguments?.getString(Constants.ADS_FROM)

        if (HomeActivity.isPromo) {
            hasSubscription = true
            handleHasSubscription()
        } else
//            HomeActivity.rcSubscriptionUserId.loginRCAppUserId {
            SubscriptionWrapper.getUserInformation { customerInfo, hasSubscription, activeSubscriptionId ->

                expirationDate = customerInfo.latestExpirationDate
                this.hasSubscription = hasSubscription
                this.activeSubscriptionId = activeSubscriptionId
                handleHasSubscription()
            }
//            }
        Handler(Looper.getMainLooper()).postDelayed({
            logEvent()
        }, 500)

        binding.viewModel = subscriptionViewModel
        binding.viewClickHandler = this


        fetchProductsAndUpdateList()

        setProductAdapter()
        setHorizontalFeaturesAdapter()
        setVerticalFeaturesAdapter()


        val text =
            "By subscribing, you agree to the displayed price. Recurring charges will continue until you cancel through Play Store settings. Your acceptance of the Terms is implied."
        val spannableString = SpannableString(text)

        val termsAndCondition = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (BaseUtils.isInternetAvailable()) {
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.TERMS_OF_SERVICES)

                    findNavController().navigate(
                        R.id.action_subscriptionFragment_to_webViewFragment,
                        bundle
                    )
                } else CommonCode.setToast(
                    requireContext(),
                    resources.getString(R.string.no_internet_msg)
                )
            }

        }

        /* val policyClickable = object : ClickableSpan() {
             override fun onClick(widget: View) {
                 if (BaseUtils.isInternetAvailable()) {
                     val bundle = Bundle()
                     bundle.putString(Constants.TYPE, Constants.PRIVACY_POLICY)

                     findNavController().navigate(
                         R.id.action_account_details_to_web_view_fragment,
                         bundle
                     )
                 } else CommonCode.setToast(
                     requireContext(),
                     resources.getString(R.string.no_internet_msg)
                 )
             }
         }*/

        //42

        spannableString.setSpan(termsAndCondition, 151, 156, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            ForegroundColorSpan(requireContext().color(R.color.lightGreyText)),
            151,
            156,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
//        spannableString.setSpan(policyClickable, 102, 116, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvTerms.highlightColor = Color.TRANSPARENT
        binding.tvTerms.text = spannableString
        binding.tvTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun handleHasSubscription() {
        if (hasSubscription) {
            binding.group.visible(isVisible = false)
            binding.tvSubscriptionActiveMsg.visible(isVisible = true)
            subscriptionViewModel.isSeeAllClicked.value = true
            if (BuildConfig.DEBUG) {
                binding.tvSubscriptionId.visible(isVisible = true)
                binding.tvSubscriptionId.text =
                    StringBuilder().append("Plan - ").append(this.activeSubscriptionId)

                expirationDate?.let {
                    binding.tvSubscriptionExpirationDate.visible(isVisible = true)
                    val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH)
                    binding.tvSubscriptionExpirationDate.text =
                        StringBuilder().append("Expiring On - ").append(outputFormat.format(it))
                }
            }
            if (!activeSubscriptionId.contains("android") && !HomeActivity.isPromo) {
                binding.tvDifferentDeviceSubTxt.visible(isVisible = true)
            } else {
                binding.tvDifferentDeviceSubTxt.visibility = View.INVISIBLE
            }
            binding.tvCancel.text = getString(R.string.go_back)
        } else {
            subscriptionViewModel.isSeeAllClicked.value = false
        }
    }

    private fun fetchProductsAndUpdateList() {
        if (subscriptionViewModel.productsList.isEmpty()) {
            SubscriptionWrapper.fetchAndDisplayAvailableProducts { list ->
                val sortedList = ArrayList<StoreProduct>(list)
                sortedList.sortBy { it.price.amountMicros }
                subscriptionViewModel.productsList.clear()
                subscriptionViewModel.revenueCatProducts.clear()

                subscriptionViewModel.revenueCatProducts.addAll(sortedList)
                subscriptionViewModel.storePricePerDuration()

                sortedList.forEachIndexed { index, storeProduct ->
                    val isSelected = index == 0
                    if (isSelected) {
                        storeProduct.updatePrice()
                    }
                    val storeProducts = StoreProducts(isSelected, storeProduct)
                    subscriptionViewModel.calculateSingleProductSavingAmount(
                        storeProduct, storeProducts
                    )
//                    subscriptionViewModel.productsList.add(storeProducts)
                }
                adapterSubscriptionOffer.updateList(subscriptionViewModel.productsList)
            }
        } else {
            adapterSubscriptionOffer.updateList(subscriptionViewModel.productsList)
        }

    }

    private fun setProductAdapter() {
        binding.rvSubscriptionItems.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.left = 20.toPx.toInt()
                } else {
                    outRect.left = 10.toPx.toInt()
                }
                outRect.top = 10.toPx.toInt()
                outRect.bottom = 10.toPx.toInt()
                outRect.right = 10.toPx.toInt()
            }
        })
        binding.rvSubscriptionItems.adapter = adapterSubscriptionOffer

        adapterSubscriptionOffer.itemSelectionCallback = { product ->
            selectedProduct = product
            product.storeProduct.updatePrice()
        }
    }

    private fun setHorizontalFeaturesAdapter() {
        binding.rvHorizontalFeatures.adapter = adapterHorizontalFeatures
    }

    private fun setVerticalFeaturesAdapter() {
        binding.rvVerticalFeatures.adapter = adapterVerticalFeatures
    }

    private fun StoreProduct.updatePrice() {
        binding.tvSubscribe.visible(isVisible = !hasSubscription)
        if (SubscriptionWrapper.activeSubscriptionId == this.id) {
            binding.tvSubscribe.text = getString(R.string.subscribed)
            binding.tvSubscribe.isEnabled = false
        } else {
            binding.tvSubscribe.isEnabled = true
            val unit = if ((period?.value ?: 0) <= 1) "${
                period?.unit?.toString()?.getStringWithFirstWordCapital()
            }" else "${period?.unit?.toString()?.getStringWithFirstWordCapital()}s"
            binding.tvSubscribe.text =
                StringBuilder().append("Get ${period?.value} $unit for ").append(price.formatted)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivClose, R.id.tvCancel -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.tvSubscribe -> {
                selectedProduct?.storeProduct?.let {
                    binding.progressBar.show()
                    binding.tvSubscribe.isEnabled = false
                    logSubscriptionInitiatedEvent(it)
                    (activity as HomeActivity?)?.makePurchase(
                        it,
                        { _, _, activeSubscriptionId ->
                            this.activeSubscriptionId = activeSubscriptionId
                            hasSubscription = activeSubscriptionId.isNotEmpty()
                            handleHasSubscription()
                            binding.progressBar.hide()
                            binding.tvSubscribe.isEnabled = true
                            if (activeSubscriptionId == it.id) {
                                binding.tvSubscribe.text = getString(R.string.subscribed)
                            }
                            RaddarApp.getInstance().setSubscriptionStatus(SubscriptionStatus.PLUS)
                            logSubscriptionPurchasedEvent(it)
                        },
                        onError = { error, userCancelled ->
                            logSubscriptionFailedEvent(it)
                            binding.progressBar.hide()
                            binding.tvSubscribe.isEnabled = true
                        })
                }
            }
        }
    }

    private fun logSubscriptionPurchasedEvent(storeProduct: StoreProduct) {
        mixPanelWrapper.logSubscriptionPurchasedEvent(JSONObject().apply {
            put(
                MixPanelWrapper.PropertiesKey.FROM_SCREEN,
                fromScreen ?: Constants.MixPanelFrom.NA
            )
            put(
                MixPanelWrapper.PropertiesKey.SUBSCRIPTION_ID,
                storeProduct.id
            )
            put(
                MixPanelWrapper.PropertiesKey.VISIT_FROM_AD_TYPE,
                if (adsFrom.isNullOrEmpty()) Constants.MixPanelFrom.OTHERS else adsFrom
            )
            put(
                MixPanelWrapper.PropertiesKey.AFTER_NO_OF_ADS_COUNT,
                getProfileViewModel.adsCountForMixpanelEvent
            )
        })
        viewLifecycleOwner.lifecycleScope.launch {
            preferencesHelper.setValue(ADS_COUNT, 0)
            getProfileViewModel.adsCountForMixpanelEvent = 0
        }
    }

    private fun logSubscriptionFailedEvent(storeProduct: StoreProduct) {
        mixPanelWrapper.logEvent(
            MixPanelWrapper.SUBSCRIPTION_PURCHASE_FAILED,
            JSONObject().apply {
                put(MixPanelWrapper.PropertiesKey.FROM_SCREEN, fromScreen)
                put(
                    MixPanelWrapper.PropertiesKey.SUBSCRIPTION_ID, storeProduct.id
                )
                put(
                    MixPanelWrapper.PropertiesKey.VISIT_FROM_AD_TYPE,
                    if (adsFrom.isNullOrEmpty()) Constants.MixPanelFrom.OTHERS else adsFrom
                )
                put(
                    MixPanelWrapper.PropertiesKey.AFTER_NO_OF_ADS_COUNT,
                    getProfileViewModel.adsCountForMixpanelEvent
                )
            }
        )
    }

    private fun logEvent() {
        mixPanelWrapper.logSubscriptionScreenVisitEvent(JSONObject().apply {
            put(MixPanelWrapper.PropertiesKey.FROM_SCREEN, fromScreen)
            put(MixPanelWrapper.PropertiesKey.SUBSCRIPTION_ID, Constants.MixPanelFrom.NA)
            put(
                MixPanelWrapper.PropertiesKey.VISIT_FROM_AD_TYPE,
                if (adsFrom.isNullOrEmpty()) Constants.MixPanelFrom.OTHERS else adsFrom
            )
            put(
                MixPanelWrapper.PropertiesKey.AFTER_NO_OF_ADS_COUNT,
                getProfileViewModel.adsCountForMixpanelEvent
            )
//            put(MixPanelWrapper.PropertiesKey.IS_SUBSCRIPTION_BOUGHT, hasSubscription)
            /*if (activeSubscriptionId.trim().isNotEmpty())
                put(MixPanelWrapper.PropertiesKey.SUBSCRIPTION_ID, activeSubscriptionId)*/
        })
    }

    private fun logSubscriptionInitiatedEvent(storeProduct: StoreProduct) {
        mixPanelWrapper.logEvent(
            MixPanelWrapper.SUBSCRIPTION_PURCHASE_INITIATED,
            JSONObject().apply {
                put(MixPanelWrapper.PropertiesKey.FROM_SCREEN, fromScreen)
                put(
                    MixPanelWrapper.PropertiesKey.AFTER_NO_OF_ADS_COUNT,
                    getProfileViewModel.adsCountForMixpanelEvent
                )
                put(
                    MixPanelWrapper.PropertiesKey.VISIT_FROM_AD_TYPE,
                    if (adsFrom.isNullOrEmpty()) Constants.MixPanelFrom.OTHERS else adsFrom
                )
                put(MixPanelWrapper.PropertiesKey.SUBSCRIPTION_ID, storeProduct.id)
            })
    }
}