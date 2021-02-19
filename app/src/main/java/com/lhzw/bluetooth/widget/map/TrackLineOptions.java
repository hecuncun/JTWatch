package com.lhzw.bluetooth.widget.map;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.lhzw.bluetooth.R;
import com.lhzw.bluetooth.application.App;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: SearchLocMap_mp
 * @Author：created by xtqb
 * @CreateDate: 2021/1/28 0028 9:02
 * @Description:
 */
public class TrackLineOptions {
    private List<LatLng> trackList;
    private Polyline polyline;
    private MapboxMap mapbox;
    private MapView mapView;
    private Polyline syncPolyline;

    public TrackLineOptions(MapView mapView, MapboxMap mapbox) {
        this.mapbox = mapbox;
        this.mapView = mapView;
        trackList = new ArrayList<>();
    }

    public TrackLineOptions() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void drawTrackLine(LatLng latLng) {
        mapView.getMapAsync(mapboxMap -> {
            trackList.add(latLng);
            if (trackList.size() < 2) {
                return;
            }
            LatLng[] latLngs = new LatLng[trackList.size()];
            int counter = 0;
            for (LatLng item : trackList) {
                latLngs[counter] = item;
                counter++;
            }
            if (polyline != null) {
                polyline.remove();
                polyline = null;
            }
            PolylineOptions options = new PolylineOptions()
                    .add(latLngs)
                    .color(App.Companion.getContext().getColor(R.color.color_red_73418))
                    .width(4);
            polyline = mapbox.addPolyline(options);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void drawTrackLine(MapView mapView, LatLng[] latLngs) {
        mapView.getMapAsync(mapboxMap -> {
            if (syncPolyline != null) {
                syncPolyline.remove();
                syncPolyline = null;
            }
            PolylineOptions options = new PolylineOptions()
                    .add(latLngs)
                    .color(App.Companion.getContext().getColor(R.color.color_red_73418))
                    .width(4);
            syncPolyline = mapboxMap.addPolyline(options);
        });
    }

    public void removeFireLine() {
        if (syncPolyline != null) {
            syncPolyline.remove();
            syncPolyline = null;
        }
    }

    public void cleanAll() {
        if (trackList.size() > 0) {
            trackList.clear();
            trackList = null;
        }
        if (polyline != null) {
            polyline.remove();
            polyline = null;
        }
        mapView = null;
        mapbox = null;
    }
}
