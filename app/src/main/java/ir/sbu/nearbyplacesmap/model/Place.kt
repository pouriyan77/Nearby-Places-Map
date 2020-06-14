package ir.sbu.nearbyplacesmap.model

import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("title") val title: String,
    @SerializedName("lat") val latitude: String,
    @SerializedName("lng") val longitude: String
) {
}