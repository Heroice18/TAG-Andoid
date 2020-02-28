package com.geo.tag

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private val markers = mutableListOf<LatLng>()
    public val newMarkers = arrayListOf<LatLng>()

    val PERMISSION_ID = 42
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //setSupportActionBar(toolbar)

        database = FirebaseDatabase.getInstance().reference
        val fireMarkers = arrayListOf<Any?>()
        database.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                //fireMarkers.add(LatLng(0.0,0.0))
                //fireMarkers.clear()

                val test = 0
                if (dataSnapshot.hasChild("Markers")) {
                    Log.d("TAG", "Working Snap Markers" );
                }
                else {
                    Log.d("TAG", "Working Snap Failed " + test);
                }

                //val coordinatesData = dataSnapshot.child()
                   fireMarkers.add(dataSnapshot.getValue())
                //val regexLatLng = "^(\-?\d+(\.\d+)?),\s*(\-?\d+(\.\d+)?)"



                /*fireMarkers.forEach(){
                    Log.d("TAG", "Data each is " + it)
                    Log.d("TAG" , "Data at this point is: " + it + " + " + test)
                    val markerValue = it.toString()
                    val result = markerValue.replace("[^(\\-?\\d+(\\.\\d+)?),\\s*(\\-?\\d+(\\.\\d+)?)]".toRegex(), "")

                    val checkResult = result.split(",").toTypedArray()
                    val first = checkResult[0].toDouble()
                    val second = checkResult[1].toDouble()
                    val coordNew = LatLng(first, second)
                    Log.d("TAG", "data coord are: " + coordNew)
                    //newMarkers.add(coordNew)

                    map.addMarker(MarkerOptions().position(coordNew).title("Marker is here"))
                }*/

                  /*  for (i in fireMarkers)
                    {
                        Log.d("TAG" , "Data at this point is: " + i + " + " + test)
                        val markerValue = i.toString()
                        val result = markerValue.replace("[^(\\-?\\d+(\\.\\d+)?),\\s*(\\-?\\d+(\\.\\d+)?)]".toRegex(), "")

                        val checkResult = result.split(",").toTypedArray()
                        val first = checkResult[0].toDouble()
                        val second = checkResult[1].toDouble()
                        val coordNew = LatLng(first, second)
                        Log.d("TAG", "data coord are: " + coordNew)
                        //newMarkers.add(coordNew)

                        map.addMarker(MarkerOptions().position(coordNew).title("Marker is here"))
                    }*/

                for (location in fireMarkers)
                {
                    Log.d("DATA", "Check location: " + location)
                    val markerValue = location.toString()
                    //val result = markerValue.replace("[^(\\-?\\d+(\\.\\d+)?),\\s*(\\-?\\d+(\\.\\d+)?)]".toRegex(), "")
                    val result = markerValue.replace(("[^\\bCoord=(-|)[0-9]*,\\s(-|)[0-9]*]").toRegex(), "")
                    Log.d("TAG", "Check location RESULT are: " + result)
                    val checkResult = result.split(",").toTypedArray()
                    Log.d("TAG", "Check location RESULT SPLIT are: " + checkResult)
                    val first = checkResult[0].toDouble()
                    val second = checkResult[1].toDouble()
                    val coordNew = LatLng(first, second)


                }

                //Log.e("TAG", "Snapshot from Firebase is: " + post)
            }

            override fun onCancelled(error: DatabaseError) {
                //print error.message
                Log.e("TAG", "Snap didn't work " + error)
            }
        })

        //database.child("Rexburg")




        val tag_BTN = findViewById<Button>(R.id.Tag_it)
        tag_BTN.setOnClickListener{
            Toast.makeText(this, "Please tap the map to place a marker",
                Toast.LENGTH_LONG).show()
            setTagBTN()
            return@setOnClickListener
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        var fusedLocationProviderClient = FusedLocationProviderClient(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    val testing = LatLng(0.0,0.0)


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    override fun onMapReady(googleMap: GoogleMap) {

        //map = googleMap
        /*val latitude = 37.422160
        val longitude = -122.084270
        val homeLatLng = LatLng(latitude, longitude)
        val zoomLevel = 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))
        setMarker(map)
        setPoiClick(map)*/
        map = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        val current = LatLng(0.0,0.0)



        val locy = ""
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        //Location() userLoc = new Location("")


        enableMyLocation()
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if(location == null) {
                // TODO, handle it
                Log.e("LOG N", "Location returned null")
            } else location.apply {
                // Handle location object
                Log.e("LOG", "CHECKING " + location.toString())
                val findLatitude = location.latitude
                val findLongitude = location.longitude
                Log.e("Test", "Lat is " + findLatitude + " Long is " + findLongitude)
                val currentPos = LatLng(findLatitude, findLongitude)
                Log.e("Test2", "Lat2 is " + currentPos)
                val level = 16.0f
                val rexTest = LatLng(43.8231, -111.7924)
                map.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 16F))


            }

        }

        //Add the user's location here

        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)

    }


    public fun setTagBTN(){
        map.setOnMapClickListener {
            map.addMarker(MarkerOptions().position(it))
            map.setOnMapClickListener(null)
        }
    }


    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            print("WORKING")
        }
        else {
            print("ASK FOR PERMISSION")
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }


    private fun setMarker(map:GoogleMap)
    {
        map.setOnMapLongClickListener {  latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("HEYO0")
                    .snippet(snippet)
            ) }
    }

    private fun setPoiClick(map:GoogleMap){
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name))
            poiMarker.showInfoWindow()
        }
    }
    override fun onMarkerClick(p0: Marker?) = false




}