package com.radarqr.dating.android.base

import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.radarqr.dating.android.data.model.report.Data
import com.radarqr.dating.android.data.model.report.SubOption
import com.radarqr.dating.android.data.model.report.SubSuboption
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import com.radarqr.dating.android.utility.handler.LocationPermissionHandler

interface BaseView {

    fun setActionBar(title: String)
    fun setTitleBackground(isSet: Boolean)
    fun showBackButton(isVisible: Boolean)
    fun showActionBar(isVisible: Boolean)
    fun showToolbar(isVisible: Boolean)
    fun showTitle(isVisible: Boolean)
    fun setTitle(text: String)

    fun showToolbarLayout(isVisible: Boolean)
    fun showBackButtonWhite(isVisible: Boolean)
    fun showTitleWhite(isVisible: Boolean)
    fun showToolbarWhite(isVisible: Boolean)
    fun setProgressbar(number: Int)
    fun showProgress(isVisible: Boolean)

    //    fun showNavigation(isVisible: Boolean)
    fun showImageDialog(action: Int)
    fun showNavigationPos(action: Int)
    fun showSkip(isVisible: Boolean)
    fun showAlert(
        message: String,
        title: String,
        okDismissClick: ((DialogInterface, Int) -> Unit?)?
    )

    fun showConfirmatonAlert(
        message: String,
        title: String,
        yesDismissClick: ((DialogInterface, Int) -> Unit?)?,
        noDismissClick: ((DialogInterface, Int) -> Unit?)?
    )

    fun showAlertWithView(view: View?): AlertDialog?
    fun onBackPress()
    fun reportApiError(
        lineNumber: Int,
        apiStatusCode: Int,
        apiName: String,
        className: String,
        errorMessage: String
    )

    fun showReportDialog()

    fun showCustomAlert(title: String, buttonText: String, clickListener: DialogClickHandler<Any>)

    fun openReportDialog(clickListener: (Data, SubOption, SubSuboption, String) -> Unit)

    fun requestNotificationPermission()

    fun isLocationPermissionGranted(handler: LocationPermissionHandler)
}