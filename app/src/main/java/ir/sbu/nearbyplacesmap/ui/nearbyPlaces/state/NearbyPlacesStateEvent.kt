package ir.sbu.nearbyplacesmap.ui.nearbyPlaces.state

sealed class NearbyPlacesStateEvent {

    class GetNearbyPlaces(val latitude: Double, val longitude: Double): NearbyPlacesStateEvent()
}