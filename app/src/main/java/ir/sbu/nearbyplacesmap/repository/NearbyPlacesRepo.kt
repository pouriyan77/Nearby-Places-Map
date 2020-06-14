package ir.sbu.nearbyplacesmap.repository

import androidx.lifecycle.LiveData
import ir.sbu.nearbyplaces.api.RetrofitBuilder
import ir.sbu.nearbyplaces.utils.ApiSuccessResponse
import ir.sbu.nearbyplaces.utils.DataState
import ir.sbu.nearbyplaces.utils.GenericApiResponse
import ir.sbu.nearbyplacesmap.api.responses.NearbyPlacesResponse
import ir.sbu.nearbyplacesmap.model.Place
import ir.sbu.nearbyplacesmap.ui.nearbyPlaces.state.NearbyPlacesViewState
import ir.sbu.nearbyplacesmap.utils.*

object NearbyPlacesRepo {

    private const val TAG = "NearbyPlacesRepo"

    fun getNearbyPlaces(lat: Double, lon: Double): LiveData<DataState<NearbyPlacesViewState>> {
        return object : NetworkBoundResource<NearbyPlacesResponse, NearbyPlacesViewState>(){
            override fun handleApiSuccessResponse(response: ApiSuccessResponse<NearbyPlacesResponse>) {
                val resBody = response.body
                val tabsContentMap = mapOf(
                    restaurantsStr to resBody.restaurants,
                    cafesStr to resBody.cafes,
                    schoolsStr to resBody.schools,
                    flowerShopsStr to resBody.flowerShops,
                    metroStationsStr to resBody.metroStations,
                    hospitalsStr to resBody.hospitals,
                    highwaysStr to resBody.highways
                )
                val viewState = NearbyPlacesViewState()
                viewState.tabsContentMap = tabsContentMap
                result.value = DataState.data(
                    data = viewState
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<NearbyPlacesResponse>> {
                return RetrofitBuilder.apiService.getNearbyPlaces(lat, lon)
            }

        }.asLiveData()
    }

}
