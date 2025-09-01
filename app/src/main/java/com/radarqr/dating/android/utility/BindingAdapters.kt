package com.radarqr.dating.android.utility

import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.utility.Utility.getCircularDrawable
import com.radarqr.dating.android.utility.Utility.getVenueUrl
import com.radarqr.dating.android.utility.Utility.listener
import com.radarqr.dating.android.utility.Utility.visible
import jp.wasabeef.glide.transformations.BlurTransformation


@BindingAdapter("imageUrl", "isCircular", "isMediumOrThumb")
fun setImageUrl(
    view: AppCompatImageView,
    imageUrl: String?,
    circular: Boolean,
    isMediumOrThumb: Boolean = false
) {
    var url = imageUrl ?: ""
    if (!url.contains("https")) {
        val map = if (isMediumOrThumb) RaddarApp.thumbOrMediumImagesMap else RaddarApp.imagesMap
        if (map.containsKey(url)) url = map[url] ?: ""
        else {
            url = S3Utils.generatesShareUrl(view.context, imageUrl).replace(" ", "%20")
            map[imageUrl ?: ""] = url
        }
    }
    Glide.with(view).load(url)
        .apply(if (circular) RequestOptions.circleCropTransform() else RequestOptions.noTransformation())
        .placeholder(
            view.getCircularDrawable(
                ContextCompat.getColor(
                    view.context,
                    R.color.teal_200
                )
            )
        ).addListener(view.listener(imageUrl ?: ""))
        .into(view)
}

@BindingAdapter("imageUrl", "isCircular", "isMediumOrThumb", "isBlured")
fun setImageUrlBlured(
    view: AppCompatImageView,
    imageUrl: String?,
    circular: Boolean,
    isMediumOrThumb: Boolean = false,
    isBlured: Boolean = false
) {
    var url = imageUrl ?: ""
    if (!url.contains("https")) {
        val map = if (isMediumOrThumb) RaddarApp.thumbOrMediumImagesMap else RaddarApp.imagesMap
        if (map.containsKey(url)) url = map[url] ?: ""
        else {
            url = S3Utils.generatesShareUrl(view.context, imageUrl).replace(" ", "%20")
            map[imageUrl ?: ""] = url
        }
    }
    Glide.with(view).load(url)
        .apply(if (circular) RequestOptions.circleCropTransform() else RequestOptions.noTransformation())
        .apply(
            if (isBlured) RequestOptions.bitmapTransform(
                BlurTransformation(
                    14,
                    6
                )
            ) else RequestOptions.noTransformation()
        )
        .placeholder(
            view.getCircularDrawable(
                ContextCompat.getColor(
                    view.context,
                    R.color.teal_200
                )
            )
        ).error({
            ContextCompat.getDrawable(view.context, R.drawable.placeholder)
        }).addListener(view.listener(imageUrl ?: ""))
        .into(view)
}

@BindingAdapter("imageAccordingToGender")
fun AppCompatImageView.setImageAccordingToGender(gender: String?) {
    gender ?: return
    setImageDrawable(
        when (gender) {
            "man", "men", Constants.MALE.lowercase() -> ContextCompat.getDrawable(
                this.context,
                R.drawable.ic_male
            )

            "woman", "women", Constants.FEMALE.lowercase() -> ContextCompat.getDrawable(
                this.context,
                R.drawable.ic_female_sign
            )

            else -> ContextCompat.getDrawable(this.context, R.drawable.ic_non_binary)
        }
    )
}

@BindingAdapter("fontType")
fun AppCompatTextView.font(type: FontsTypes) {
    try {
        typeface = ResourcesCompat.getFont(context, type.fontRes)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@BindingAdapter("contentType")
fun RecyclerView.handleVisibility(type: EditProfileGeneralContentTypes) {
    visible(
        !(type == EditProfileGeneralContentTypes.NAME
                || type == EditProfileGeneralContentTypes.AGE
                || type == EditProfileGeneralContentTypes.HEIGHT
                || type == EditProfileGeneralContentTypes.JOB_TITLE
                || type == EditProfileGeneralContentTypes.AGE_RANGE
                || type == EditProfileGeneralContentTypes.HEIGHT_PREF
                || type == EditProfileGeneralContentTypes.MAX_DISTANCE)
    )
}