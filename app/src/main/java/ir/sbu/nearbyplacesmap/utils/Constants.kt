package ir.sbu.nearbyplacesmap.utils

import android.content.Context
import android.util.DisplayMetrics
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt


const val BASE_URL = "https://bo.hichestan.org/rest/hmj/"

val DEFAULT_POSITION = LatLng(35.795314, 51.398217) // Tehran Velenjak
const val allStr = "همه"
const val restaurantsStr = "رستوران"
const val cafesStr = "کافی شاپ"
const val schoolsStr = "مدرسه"
const val flowerShopsStr = "گلفروشی"
const val metroStationsStr = "مترو"
const val hospitalsStr = "بیمارستان"
const val highwaysStr = "بزرگراه"

fun dpToPx(dp: Int, context: Context): Int {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}