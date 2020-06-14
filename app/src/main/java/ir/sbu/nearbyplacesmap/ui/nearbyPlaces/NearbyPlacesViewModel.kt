package ir.sbu.nearbyplacesmap.ui.nearbyPlaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ir.sbu.nearbyplaces.utils.DataState
import ir.sbu.nearbyplacesmap.model.Place
import ir.sbu.nearbyplacesmap.repository.NearbyPlacesRepo
import ir.sbu.nearbyplacesmap.ui.nearbyPlaces.state.NearbyPlacesStateEvent
import ir.sbu.nearbyplacesmap.ui.nearbyPlaces.state.NearbyPlacesViewState
import ir.sbu.nearbyplacesmap.utils.allStr

class NearbyPlacesViewModel : ViewModel() {
    val viewState = MutableLiveData<NearbyPlacesViewState>()
    private var stateEvent = MutableLiveData<NearbyPlacesStateEvent>()

    val dataState = Transformations
        .switchMap(stateEvent) {
            handleStateEvent(it)
        }

    private fun handleStateEvent(stateEvent: NearbyPlacesStateEvent): LiveData<DataState<NearbyPlacesViewState>> {
        when (stateEvent) {
            is NearbyPlacesStateEvent.GetNearbyPlaces -> {
                return NearbyPlacesRepo.getNearbyPlaces(stateEvent.latitude, stateEvent.longitude)
            }
        }
    }

    fun setStateEvent(stateEvent: NearbyPlacesStateEvent) {
        this.stateEvent.value = stateEvent
    }

    fun setTabContentMap(tabsContentMap: Map<String, List<Place>?>) {
        val updatedViewState = viewState.value ?: NearbyPlacesViewState()
        updatedViewState.tabsContentMap = tabsContentMap
        viewState.value = updatedViewState
    }

    fun getTabContentMap(): Map<String, List<Place>?>? {
        return viewState.value?.tabsContentMap
    }

    fun getCategoryPlaces(category: String): List<Place>? {
        if (category == allStr) {
            val allPlaces = mutableListOf<Place>()
            viewState.value?.tabsContentMap?.values?.forEach { placeList ->
                placeList?.let {
                    allPlaces.addAll(it)
                }
            }
            return allPlaces
        }
        return viewState.value?.tabsContentMap?.get(category)
    }
}