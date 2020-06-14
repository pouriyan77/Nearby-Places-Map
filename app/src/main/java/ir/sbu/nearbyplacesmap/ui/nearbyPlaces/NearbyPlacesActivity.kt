package ir.sbu.nearbyplacesmap.ui.nearbyPlaces

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import ir.sbu.nearbyplacesmap.R
import ir.sbu.nearbyplacesmap.adapter.PlacesRecyclerAdapter
import ir.sbu.nearbyplacesmap.model.Place
import ir.sbu.nearbyplacesmap.ui.nearbyPlaces.state.NearbyPlacesStateEvent
import ir.sbu.nearbyplacesmap.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class NearbyPlacesActivity : AppCompatActivity(), OnMapReadyCallback,
    TabLayout.OnTabSelectedListener {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel: NearbyPlacesViewModel by viewModels()
    private lateinit var gMap: GoogleMap
    private lateinit var myTabLayout: TabLayout
    private var marker: Marker? = null
    private var lastOpened: Marker? = null
    private var lastLatLong: LatLng? = null
    private var pinMarkerList = mutableListOf<Marker>()
    private var pinMarkersMap = mutableMapOf<String, List<Marker>>()
    private lateinit var categoryTitleTextView: TextView
    private lateinit var foundedTextView: TextView
    private lateinit var placesAdapter: PlacesRecyclerAdapter
    private lateinit var mapViewParams: LinearLayout.LayoutParams
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        subscribeObservers()
        (map as SupportMapFragment).getMapAsync(this)
    }

    private fun setupViews() {
        myTabLayout = tabLayout
        categoryTitleTextView = categoryTitleText
        foundedTextView = foundedText
        placesRecycler.apply {
            placesAdapter = PlacesRecyclerAdapter()
            adapter = placesAdapter
        }
        mapViewParams = map?.view?.layoutParams as LinearLayout.LayoutParams
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                val h = bottomSheet.height.toFloat()
//                val off = h * slideOffset
//
//                when (bottomSheetBehavior.state) {
//                    BottomSheetBehavior.STATE_DRAGGING -> {
//                        setMapViewParams(off.roundToInt())
//                    }
//                    BottomSheetBehavior.STATE_SETTLING -> {
//                        setMapViewParams(off.roundToInt())
//                    }
//                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    setMapViewParams(false)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    setMapViewParams(true)
                }
            }

        })
    }

//    private fun setMapViewParams(offset: Int) {
//        mapViewParams.bottomMargin = offset
//        map?.view?.layoutParams = mapViewParams
//    }
//
    private fun setMapViewParams(isCollapsed: Boolean) {
        mapViewParams.bottomMargin = resources.getDimensionPixelOffset(
            if (isCollapsed)
                R.dimen.bottom_sheet_collapse_height
            else
                R.dimen.bottom_sheet_expand_height
        )
        map?.view?.layoutParams = mapViewParams
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(this, Observer { dataState ->

            showProgress(dataState.loading)

            dataState.message?.getContentIfNotHandled()?.let {
                Snackbar.make(root, "Network Error", 1000).show()
            }

            dataState.data?.let { event ->
                event.getContentIfNotHandled()?.let { viewState ->
                    viewModel.setTabContentMap(viewState.tabsContentMap!!)
                }
            }
        })

        viewModel.viewState.observe(this, Observer { viewState ->
            viewState.tabsContentMap?.let { tabsContentMap ->
                lastLatLong = marker?.position
                resetMapMarkers()
                myTabLayout.getTabAt(0)?.select()
            }
        })
    }

    private fun setupTabLayout() {
        resetTabs()
        myTabLayout.addOnTabSelectedListener(this)
    }

    private fun resetTabs() {
        myTabLayout.removeAllTabs()
        addTab(allStr)
        addTab(restaurantsStr)
        addTab(cafesStr)
        addTab(schoolsStr)
        addTab(flowerShopsStr)
        addTab(metroStationsStr)
        addTab(hospitalsStr)
        addTab(highwaysStr)
    }

    private fun addTab(title: String): TabLayout.Tab {
        val tab = myTabLayout.newTab().setText(title)
        myTabLayout.addTab(tab)
        return tab
    }

    private fun showProgress(loading: Boolean) {
        if (loading) {
            loadingProgress.visibility = View.VISIBLE
        } else {
            loadingProgress.visibility = View.GONE
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        this.gMap = map!!
        setupTabLayout()
        setupCamera()
        setupMarkersClickListener()
    }

    private fun setupMarkersClickListener() {
        gMap.setOnMarkerClickListener(OnMarkerClickListener { marker -> // Check if there is an open info window
            if (lastOpened != null) {
                // Close the info window
                lastOpened?.hideInfoWindow()

                // Is the marker the same marker that was already open
                if (lastOpened!! == marker) {
                    // Nullify the lastOpened object
                    lastOpened = null
                    // Return so that the info window isn't opened again
                    return@OnMarkerClickListener true
                }
            }

            // Open the info window for the marker
            marker.showInfoWindow()
            // Re-assign the last opened such that we can close it later
            lastOpened = marker

            // Event was handled by our code do not launch default behaviour.
            true
        })
    }

    private fun setupCamera() {
        gMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_POSITION))
        gMap.setMaxZoomPreference(16f)
        gMap.setMinZoomPreference(12f)

        gMap.setOnCameraIdleListener {
            Log.d(TAG, "onCameraIdle")
            val cameraPosition = gMap.cameraPosition.target
            marker?.remove()
            marker = gMap.addMarker(MarkerOptions().position(cameraPosition))
            val distance = FloatArray(1)

            lastLatLong?.let {
                Location.distanceBetween(
                    it.latitude,
                    it.longitude,
                    cameraPosition.latitude,
                    cameraPosition.longitude,
                    distance
                )
            }

            if (lastLatLong == null || distance[0] >= 1000f) {
                triggerGetNearbyPlacesEvent(cameraPosition)
            }
        }

        gMap.setOnCameraMoveStartedListener {
            Log.d(TAG, "OnCameraMoveStarted")
        }
    }

    private fun triggerGetNearbyPlacesEvent(latLong: LatLng) {
        viewModel.setStateEvent(
            NearbyPlacesStateEvent.GetNearbyPlaces(
                latLong.latitude,
                latLong.longitude
            )
        )
    }

    private fun resetMapMarkers() {
        val oldList = pinMarkerList
        pinMarkerList = mutableListOf()
        pinMarkersMap = mutableMapOf()
        oldList.forEach {
            CoroutineScope(Main).launch {
                it.remove()
            }
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabReselected")
        setupMarkersAndBottomSheet(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabUnselected")
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabSelected")
        setupMarkersAndBottomSheet(tab)
    }

    private fun setupMarkersAndBottomSheet(tab: TabLayout.Tab?) {
        val categoryTitle = tab?.text.toString()
        filterMarkersOrRegenerate(categoryTitle)
        setupBottomSheet(categoryTitle)
    }

    private fun setupBottomSheet(category: String) {
        categoryTitleTextView.text = category
        val placeList = viewModel.getCategoryPlaces(category)
        foundedTextView.text = String.format(
            getString(R.string.foundedStr),
            placeList?.size ?: 0,
            if (category != allStr) category else "مکان"
        )
        placesAdapter.submitList(placeList)
    }

    private fun filterMarkersOrRegenerate(category: String) {
        if (pinMarkerList.isEmpty()) {
            regenerateMarkers()
        } else {
            filterMarkers(category)
        }
    }

    private fun regenerateMarkers() {
        val markerOption = MarkerOptions()
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_ic))
        val tabContentMap = viewModel.getTabContentMap()
        tabContentMap?.keys?.forEach { key ->
            val placeList = tabContentMap[key]
            val markerList = mutableListOf<Marker>()
            pinMarkersMap[key] = markerList
            placeList?.forEach { place ->
                addMarkerParallel(place, markerOption, markerList)
            }
        }
    }

    private fun filterMarkers(category: String) {
        if (category == allStr){
            pinMarkerList.forEach {
                it.isVisible = true
            }
        } else {
            pinMarkersMap.keys.forEach {
                if (it == category) {
                    pinMarkersMap[it]?.forEach {
                        it.isVisible = true
                    }
                } else {
                    pinMarkersMap[it]?.forEach {
                        it.isVisible = false
                    }
                }
            }
        }
    }

    private fun addMarkerParallel(place: Place, markerOption: MarkerOptions, markerList: MutableList<Marker>) {
        CoroutineScope(Main).launch {
            val marker = gMap.addMarker(
                markerOption
                    .position(LatLng(place.latitude.toDouble(), place.longitude.toDouble()))
                    .title(place.title)
            )
            pinMarkerList.add(marker)
            markerList.add(marker)
        }
    }
}
