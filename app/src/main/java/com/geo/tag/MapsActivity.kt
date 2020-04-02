package com.geo.tag

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Constraints
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.alert_dialog_text.*
import java.util.*
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private val markers = mutableListOf<LatLng>()
    private val mapBathroom = mutableListOf<Marker>()
    private val mapEducational = mutableListOf<Marker>()
    private val mapHealth = mutableListOf<Marker>()
    private val mapPark = mutableListOf<Marker>()
    public val newMarkers = arrayListOf<LatLng>()
    public var markerTitle = String()

    val PERMISSION_ID = 42
    override fun onCreate(savedInstanceState: Bundle?) {
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        connectDatabase()

        setImageClick()

        val tag_BTN = findViewById<ImageButton>(R.id.Tag_it)
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


    /*
    This function adds the tag to the map
    */

    public fun setTagBTN(){

        map.setOnMapClickListener {

            val inflater = layoutInflater

            val confirmTag = AlertDialog.Builder(this)
            confirmTag.setMessage("Are you sure you want to place a tag at this location?")

            val dialogLayout = inflater.inflate(R.layout.alert_dialog_text, null)

            val editText = dialogLayout.findViewById<EditText>(R.id.editTextAlert)
            confirmTag.setView(dialogLayout)
            val selectedFilter = dialogLayout.findViewById<RadioGroup>(R.id.filterGroup)
            selectedFilter.check(R.id.Bathroom)




            //val selectedFilterOption = resources.getResourceEntryName(filterID)



            //val group = findViewById<RadioGroup>(R.id.filterGroup)
            //val groupId = group.checkedRadioButtonId

            /*if(group != null) {
                group.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener() { radioGroup, i ->
                    Toast.makeText(
                        this, "Selected filter is " + i,
                        Toast.LENGTH_LONG
                    ).show()
                }

                )
            }*/

            /*val dView = getLayoutInflater().inflate(R.layout.alert_dialog_text, null)
            val grouped = dView.findViewById<RadioGroup>(R.id.filterGroup)
            grouped.setOnCheckedChangeListener(
                RadioGroup.OnCheckedChangeListener { group, checkedId ->
                    val selectedButton = grouped.findViewById<RadioButton>(checkedId)
                    Log.d("TAG", "Filter Selected is " + selectedButton)
                })*/


                confirmTag.setPositiveButton("Confirm")
                { dialoginterface, i ->

                    val filterID = selectedFilter.checkedRadioButtonId
                    val filterIDString = resources.getResourceEntryName(filterID)
                    Log.d("TAG", "Selected Filter is: " + filterIDString)

                    markerTitle = editText.text.toString()

                    val addMarker = markerToData(it, markerTitle, filterIDString)
                    val check = it
                    Log.d("DAD", "Coord are:  " + check)
                    database.child("Markers").child(markerTitle).setValue(addMarker)

                    Toast.makeText(this, "Please select a filter",
                        Toast.LENGTH_LONG).show()


                    if(filterIDString == "Bathroom") {
                        val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor))
                        mapBathroom.add(map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor)))
                    }
                    else if(filterIDString == "Educational")
                    {
                        val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
                        map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor))
                        mapEducational.add(map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor)))
                    }
                    else if (filterIDString == "Health")
                    {
                        val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                        map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor))
                        mapHealth.add(map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor)))
                    }
                    else if (filterIDString == "Parks")
                    {
                        val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor))
                        mapPark.add(map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor)))
                    }
                    else
                    {
                        val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        map.addMarker(MarkerOptions().position(it).title(markerTitle).snippet("Filter: " + filterIDString).icon(markerColor))
                    }

                    Toast.makeText(
                        applicationContext,
                        "Thanks! You've tagged " + markerTitle,
                        Toast.LENGTH_SHORT
                    ).show()

                }

            confirmTag.show()
            //val confirmButton = confirmTag.setPositiveButton()
            map.setOnMapClickListener(null)
        }
    }




    data class markerToData(
        var coordinate: LatLng? = LatLng(0.0,0.0),
        var title: String? = "",
        var filter: String? = ""

    )


    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }

    private fun connectDatabase(){
        database = FirebaseDatabase.getInstance().reference
        val fireMarkers = arrayListOf<Any?>()
        val coordMarkers = arrayListOf<Any?>()
        val markerLocation = database.child("Markers")
        markerLocation.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val test = 0
                if (dataSnapshot.hasChild("Markers")) {
                    Log.d("TAG", "Working Snap Markers" );
                }
                else {
                    Log.d("TAG", "Working Snap Failed " + test);
                }



                val regexCordinates = "[^\\b(-|)[0-9]*,\\s(-|)[0-9]*[0-9]]"

                if(dataSnapshot.hasChildren()) {
                    for (ds in dataSnapshot.children) {
                        Log.d("TAG", "Check the DS: " + ds)
                        val markerData = ds.getValue().toString()

                        val coordData = markerData.substringAfter("Coord=".substringBefore("Title"))

                        val res = markerData.replace("[^0-9,-.]".toRegex(), "")
                        Log.d("TAG", "Check the replace: " + res)
                        val resFinal = res.substring(1)

                        val titleData = markerData.substringAfter("title=")
                        val titleSplit = titleData.replace("}", "")
                        Log.d("TAG", "Check title: " + titleSplit)

                        val filterData = markerData.substringAfter("filter=")
                        //val filterSplit = filterData.replace("", "")
                        val filterSplit = filterData.substringBefore(",")
                        Log.d("TAG", "Check filter: " + filterSplit)

                        val worldMap = resFinal.split(",")
                        Log.e("Check", "WorldMap is: " + worldMap)
                        val coordLat = worldMap[0].toDouble()
                        val coordLon = worldMap[1].toDouble()

                        Log.d(
                            "TAG",
                            "Check total data, latitude: " + coordLat + " Longitude: " + coordLon
                        )

                        if(filterSplit == "Bathroom") {
                            val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                            map.addMarker(
                                MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                    titleSplit
                                ).snippet("Filter: " + filterSplit).icon(markerColor)
                            )
                            mapBathroom.add(map.addMarker(MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                titleSplit
                            ).snippet("Filter: " + filterSplit).icon(markerColor)
                            ))
                        }
                        else if(filterSplit == "Educational")
                        {
                            val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
                            map.addMarker(
                                MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                    titleSplit
                                ).snippet("Filter: " + filterSplit).icon(markerColor)
                            )
                            mapEducational.add(map.addMarker(MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                titleSplit
                            ).snippet("Filter: " + filterSplit).icon(markerColor)
                            ))
                        }
                        else if (filterSplit == "Health")
                        {
                            val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                            map.addMarker(
                                MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                    titleSplit
                                ).snippet("Filter: " + filterSplit).icon(markerColor)
                            )
                            mapHealth.add(map.addMarker(MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                titleSplit
                            ).snippet("Filter: " + filterSplit).icon(markerColor)))
                        }
                        else if (filterSplit == "Parks")
                        {
                            val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                            map.addMarker(
                                MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                    titleSplit
                                ).snippet("Filter: " + filterSplit).icon(markerColor)
                            )
                            mapPark.add(map.addMarker( MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                titleSplit
                            ).snippet("Filter: " + filterSplit).icon(markerColor)))
                        }
                        else
                        {
                            val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            map.addMarker(
                                MarkerOptions().position(LatLng(coordLat, coordLon)).title(
                                    titleSplit
                                ).snippet("Filter: " + filterSplit).icon(markerColor)
                            )
                        }




                        Log.d("TAG", "Check the Substring: " + coordData)
                        Log.d("TAG", "Check the Data: " + markerData)
                    }

                    fireMarkers.add(dataSnapshot.getValue())


                    fireMarkers.groupBy { it }.forEach { it, coordMarkers ->
                        Log.d("TAG", "Checking for data:  " + it)
                    }

                }
                else
                {

                }

            }

            override fun onCancelled(error: DatabaseError) {
                //print error.message
                Log.e("TAG", "Snap didn't work " + error)
            }
        })

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

    private fun setImageClick(){
        val bathroomImage = findViewById<ImageView>(R.id.bathroomImg)
        bathroomImage.setOnClickListener{

            map.clear()
            for (mark in mapBathroom)
            {
                val markPos = mark.position
                Log.d("TAG", "Mark pos: " + markPos)
                val markTitle = mark.title
                Log.d("TAG", "Mark Title: " + markTitle)
                val markSnip = mark.snippet
                Log.d("TAG", "Mark snippet: " + markSnip)
                val markIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)

                map.addMarker(MarkerOptions().position(markPos).title(markTitle).snippet(markSnip).icon(markIcon))
            }
            Toast.makeText(this@MapsActivity, "You clicked on Bathroom.", Toast.LENGTH_SHORT).show()
        }
        val educationalImage = findViewById<ImageView>(R.id.educationalImg)
        educationalImage.setOnClickListener {

            map.clear()
            for (mark in mapEducational)
            {
                val markPos = mark.position
                Log.d("TAG", "Mark pos: " + markPos)
                val markTitle = mark.title
                Log.d("TAG", "Mark Title: " + markTitle)
                val markSnip = mark.snippet
                Log.d("TAG", "Mark snippet: " + markSnip)
                val markIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)

                map.addMarker(MarkerOptions().position(markPos).title(markTitle).snippet(markSnip).icon(markIcon))
            }
            Toast.makeText(this@MapsActivity, "You clicked on Bathroom.", Toast.LENGTH_SHORT).show()



            Toast.makeText(this@MapsActivity, "You clicked on Educational.", Toast.LENGTH_SHORT).show()
        }
        val parksImage= findViewById<ImageView>(R.id.parksImg)
        parksImage.setOnClickListener {

            map.clear()
            for (mark in mapPark)
            {
                val markPos = mark.position
                Log.d("TAG", "Mark pos: " + markPos)
                val markTitle = mark.title
                Log.d("TAG", "Mark Title: " + markTitle)
                val markSnip = mark.snippet
                Log.d("TAG", "Mark snippet: " + markSnip)
                val markIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)

                map.addMarker(MarkerOptions().position(markPos).title(markTitle).snippet(markSnip).icon(markIcon))
            }
            Toast.makeText(this@MapsActivity, "You clicked on Bathroom.", Toast.LENGTH_SHORT).show()


            Toast.makeText(this@MapsActivity, "You clicked on Parks.", Toast.LENGTH_SHORT).show()
        }
        val healthImage = findViewById<ImageView>(R.id.healthImg)
        healthImage.setOnClickListener {

            map.clear()
            for (mark in mapHealth)
            {
                val markPos = mark.position
                Log.d("TAG", "Mark pos: " + markPos)
                val markTitle = mark.title
                Log.d("TAG", "Mark Title: " + markTitle)
                val markSnip = mark.snippet
                Log.d("TAG", "Mark snippet: " + markSnip)
                val markIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)

                map.addMarker(MarkerOptions().position(markPos).title(markTitle).snippet(markSnip).icon(markIcon))
            }
            Toast.makeText(this@MapsActivity, "You clicked on Bathroom.", Toast.LENGTH_SHORT).show()


            Toast.makeText(this@MapsActivity, "You clicked on Hospital.", Toast.LENGTH_SHORT).show()
        }


        val clearImage = findViewById<ImageView>(R.id.clearImg)
        clearImage.setOnClickListener {
            map.clear()
            for (mark in mapHealth)
            {
                val markPos = mark.position
                Log.d("TAG", "Mark pos: " + markPos)
                val markTitle = mark.title
                Log.d("TAG", "Mark Title: " + markTitle)
                val markSnip = mark.snippet
                Log.d("TAG", "Mark snippet: " + markSnip)
                val markIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)

                map.addMarker(MarkerOptions().position(markPos).title(markTitle).snippet(markSnip).icon(markIcon))
            }

            for (mark in mapPark)
            {
                val markPos = mark.position
                Log.d("TAG", "Mark pos: " + markPos)
                val markTitle = mark.title
                Log.d("TAG", "Mark Title: " + markTitle)
                val markSnip = mark.snippet
                Log.d("TAG", "Mark snippet: " + markSnip)
                val markIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)

                map.addMarker(MarkerOptions().position(markPos).title(markTitle).snippet(markSnip).icon(markIcon))
            }

            for (mark in mapEducational)
            {
                val markPos = mark.position
                Log.d("TAG", "Mark pos: " + markPos)
                val markTitle = mark.title
                Log.d("TAG", "Mark Title: " + markTitle)
                val markSnip = mark.snippet
                Log.d("TAG", "Mark snippet: " + markSnip)
                val markIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)

                map.addMarker(MarkerOptions().position(markPos).title(markTitle).snippet(markSnip).icon(markIcon))
            }

            for (mark in mapBathroom)
            {
                val markPos = mark.position
                Log.d("TAG", "Mark pos: " + markPos)
                val markTitle = mark.title
                Log.d("TAG", "Mark Title: " + markTitle)
                val markSnip = mark.snippet
                Log.d("TAG", "Mark snippet: " + markSnip)
                val markIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)

                map.addMarker(MarkerOptions().position(markPos).title(markTitle).snippet(markSnip).icon(markIcon))
            }
            Toast.makeText(this@MapsActivity, "You clicked on Clear Filters.", Toast.LENGTH_SHORT).show()

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