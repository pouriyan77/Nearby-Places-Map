package ir.sbu.nearbyplacesmap.api.responses

import com.google.gson.annotations.SerializedName
import ir.sbu.nearbyplacesmap.model.Place

class NearbyPlacesResponse(
    @SerializedName("restaurants")
    val restaurants: List<Place>?,
    @SerializedName("cafes")
    val cafes: List<Place>?,
    @SerializedName("schools")
    val schools: List<Place>?,
    @SerializedName("flower_shops")
    val flowerShops: List<Place>?,
    @SerializedName("metro_stations")
    val metroStations: List<Place>?,
    @SerializedName("hospitals")
    val hospitals: List<Place>?,
    @SerializedName("highways")
    val highways: List<Place>?
)