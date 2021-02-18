package com.lhzw.bluetooth.base

import android.os.Bundle
import com.mapbox.mapboxsdk.maps.MapView

/**
 * Created by heCunCun on 2021/2/18
 */
abstract class BaseMapActivity:BaseActivity() {

    abstract fun getMapView():MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMapView().onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getMapView().onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        getMapView().onStart()
    }

    override fun onResume() {
        super.onResume()
        getMapView().onResume()
    }

    override fun onStop() {
        super.onStop()
        getMapView().onStop()
    }

    override fun onPause() {
        super.onPause()
        getMapView().onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        getMapView().onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        getMapView().onDestroy()
    }

}