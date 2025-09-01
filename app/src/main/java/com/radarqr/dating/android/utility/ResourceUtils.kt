package com.quickblox.sample.chat.kotlin.utils

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import java.util.*


private const val RANDOM_COLOR_START_RANGE = 0
private const val RANDOM_COLOR_END_RANGE = 9

private const val COLOR_MAX_VALUE = 255
private val COLOR_ALPHA = 0.8f

private val colorsMap = HashMap<Int, Int>()
private val random = Random()

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun getColorCircleDrawable(colorPosition: Int): Drawable {
    return getColoredCircleDrawable(getCircleColor(colorPosition % RANDOM_COLOR_END_RANGE))
}

private fun getCircleColor(@IntRange(from = RANDOM_COLOR_START_RANGE.toLong(), to = RANDOM_COLOR_END_RANGE.toLong())
                           colorPosition: Int): Int {
    val colorIdName = String.format("random_color_%d", colorPosition + 1)
    val colorId = RaddarApp.getInstance().resources.getIdentifier(colorIdName, "color", RaddarApp.getInstance().packageName)
    return RaddarApp.getInstance().resources.getColor(colorId)
}

fun getGreyCircleDrawable(): Drawable {
    return getColoredCircleDrawable(RaddarApp.getInstance().resources.getColor(R.color.text_light))
}

private fun getColoredCircleDrawable(@ColorInt color: Int): Drawable {
    val drawable = RaddarApp.getInstance().resources.getDrawable(R.drawable.green_round_thumb) as GradientDrawable
    drawable.setColor(color)
    return drawable
}

fun getRandomTextColorById(senderId: Int): Int {
    if (colorsMap[senderId] == null) {
        val colorValue = getRandomColor()
        colorsMap[senderId] = colorValue
    }
    return colorsMap[senderId]!!
}

private fun getRandomColor(): Int {
    val hsv = FloatArray(3)
    var color = Color.argb(COLOR_MAX_VALUE, random.nextInt(COLOR_MAX_VALUE),
            random.nextInt(COLOR_MAX_VALUE), random.nextInt(COLOR_MAX_VALUE))
    Color.colorToHSV(color, hsv)
    hsv[2] *= COLOR_ALPHA
    color = Color.HSVToColor(hsv)
    return color
}