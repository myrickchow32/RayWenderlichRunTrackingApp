package com.rwRunTrackingApp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

  private lateinit var mMap: GoogleMap
  var initialStepCount = -1
  lateinit var fusedLocationProviderClient: FusedLocationProviderClient
  val polylineOptions = PolylineOptions()
  var currentNumberOfStepCount = 0
  var totalDistanceTravelled = 0f
  var lastKnownLocation: Location? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_maps)
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    startButton.setOnClickListener { startButtonClicked() }
    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)
  }

  @SuppressLint("MissingPermission")
  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap

    runWithLocationPermissionChecking {
      mMap.isMyLocationEnabled = true
    }

    // Add a marker in Hong Kong and move the camera
    val latitude = 22.3193
    val longitude = 114.1694
    val hongKongLatLong = LatLng(latitude, longitude)

    val zoomLevel = 9.5f
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hongKongLatLong, zoomLevel))
  }

  fun startButtonClicked() {
    RxPermissions(this).request(Manifest.permission.ACTIVITY_RECOGNITION)
      .subscribe { isGranted ->
        Log.d("TAG", "Is ACTIVITY_RECOGNITION permission granted: $isGranted")
        if (isGranted) {
          setupStepCounterListener()
        }
      }
    setupLocationChangeListener()
  }

  fun setupStepCounterListener() {
    val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    stepCounterSensor?.let {
      sensorManager.registerListener(this@MapsActivity, it, SensorManager.SENSOR_DELAY_FASTEST)
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    Log.d("TAG", "onAccuracyChanged: Sensor: $sensor; accuracy: $accuracy")
  }

  override fun onSensorChanged(sensorEvent: SensorEvent?) {
    Log.d("TAG", "onSensorChanged")
    sensorEvent ?: return
    sensorEvent.values.firstOrNull()?.let {
      Log.d("TAG", "Step count: $it ")
      if (initialStepCount == -1) {
        initialStepCount = it.toInt()
      }
      currentNumberOfStepCount = it.toInt() - initialStepCount
      averagePaceTextView.text = "Average pace: ${totalDistanceTravelled / currentNumberOfStepCount.toDouble()} m/ step"
      numberOfStepTextView.text = "Step count: $currentNumberOfStepCount"
    }
  }

  private fun runWithLocationPermissionChecking(callback: () -> Unit) {
    RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION)
      .subscribe { isGranted ->
        if (isGranted) callback() else Toast.makeText(this, "Please grant Location permission", Toast.LENGTH_LONG).show()
      }
  }

  @SuppressLint("MissingPermission")
  fun setupLocationChangeListener() {
    runWithLocationPermissionChecking {
      val locationRequest = LocationRequest()
      locationRequest.interval = 5000

      val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
          super.onLocationResult(locationResult)
          locationResult ?: return

          locationResult.locations.forEach {
            Log.d("TAG", "New location got: (${it.latitude}, ${it.longitude})")
          }

          locationResult.locations.forEach {
            if (lastKnownLocation == null) {
              lastKnownLocation = it
              return@forEach
            }
            totalDistanceTravelled = totalDistanceTravelled + it.distanceTo(lastKnownLocation)
          }
          addLocationToRoute(locationResult.locations)
          totalDistanceTextView.text = "Total distance: ${totalDistanceTravelled}m"
          if (currentNumberOfStepCount != 0) {
            averagePaceTextView.text = "Average pace: ${totalDistanceTravelled / currentNumberOfStepCount} m/ step"
          }
        }
      }
      fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
  }

  fun addLocationToRoute(locations: List<Location>) {
    mMap.clear()
    val originalLatLngList = polylineOptions.points
    val latLngList = locations.map {
      LatLng(it.latitude, it.longitude)
    }
    originalLatLngList.addAll(latLngList)
    mMap.addPolyline(polylineOptions)
  }
}