package com.example.lab14

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

import com.google.maps.android.PolyUtil
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mPolyline: Polyline? = null
    private var mGeoApiContext: GeoApiContext? = null
    private val taipei101 = LatLng(25.033611, 121.565000)
    private val taipeiMainStation = LatLng(25.047924, 121.517081)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync(this)

        val btnDriving = findViewById<Button>(R.id.btnDriving)
        val btnWalking = findViewById<Button>(R.id.btnWalking)
        val btnBicycling = findViewById<Button>(R.id.btnBicycling)

        btnDriving.setOnClickListener {
            getRoute(TravelMode.DRIVING)
        }
        btnWalking.setOnClickListener {
            getRoute(TravelMode.WALKING)
        }
        btnBicycling.setOnClickListener {
            getRoute(TravelMode.BICYCLING)
        }
    }

    override fun onMapReady(map: GoogleMap) {

        mMap = map

        val isAccessFineLocationGranted =
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val isAccessCoarseLocationGranted =
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (isAccessFineLocationGranted && isAccessCoarseLocationGranted) {
            map.isMyLocationEnabled = true

            map.addMarker(MarkerOptions().position(taipei101).title("台北101"))
            map.addMarker(MarkerOptions().position(taipeiMainStation).title("台北車站"))

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(25.04, 121.54), 13f))

            val apiKey = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                .metaData.getString("com.google.android.geo.API_KEY")

            if (apiKey.isNullOrEmpty()) {
                return
            }

            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(apiKey)
                .build()

            getRoute(TravelMode.WALKING)

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0
            )
        }
    }

    private fun getRoute(mode: TravelMode) {

        if (mMap == null || mGeoApiContext == null) {
            return
        }

        mPolyline?.remove()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val directionsResult =
                    DirectionsApi.newRequest(mGeoApiContext)
                        .origin(com.google.maps.model.LatLng(taipei101.latitude, taipei101.longitude))
                        .destination(com.google.maps.model.LatLng(taipeiMainStation.latitude, taipeiMainStation.longitude))
                        .mode(mode)
                        .await()

                CoroutineScope(Dispatchers.Main).launch {
                    if (directionsResult.routes.isNotEmpty()) {
                        val route = directionsResult.routes[0]

                        val decodedPath =
                            PolyUtil.decode(route.overviewPolyline.encodedPath)

                        val polylineOptions = PolylineOptions()
                            .addAll(decodedPath)
                            .color(Color.RED)
                            .width(15f)

                        mPolyline = mMap?.addPolyline(polylineOptions)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}