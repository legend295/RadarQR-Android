package com.radarqr.dating.android.ui.subscription.model

import com.revenuecat.purchases.models.StoreProduct

data class StoreProducts(
    var isSelected: Boolean,
    var storeProduct: StoreProduct,
    var key: String? = null,
    var savingPercentage: Int? = null,
    var amountPerWeek: String? = null
)
