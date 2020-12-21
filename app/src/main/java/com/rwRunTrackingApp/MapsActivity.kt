package com.rwRunTrackingApp

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

  private lateinit var mMap: GoogleMap
  var initialStepCount = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_maps)
    startButton.setOnClickListener { startButtonClicked() }
    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)
  }

  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap

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
      numberOfStepTextView.text = "Step count: ${it.toInt() - initialStepCount}"
    }
  }

}