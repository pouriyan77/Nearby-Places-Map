package ir.sbu.nearbyplaces.api

import androidx.lifecycle.LiveData
import ir.sbu.nearbyplaces.utils.GenericApiResponse
import ir.sbu.nearbyplacesmap.api.responses.NearbyPlacesResponse
import ir.sbu.nearbyplacesmap.model.Place
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("by/point/{lat}/{lon}")
    fun getNearbyPlaces(
        @Path("lat") latitude: Double,
        @Path("lon") longitude: Double
    ): LiveData<GenericApiResponse<NearbyPlacesResponse>>

}