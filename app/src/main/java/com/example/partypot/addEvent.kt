package com.example.partypot

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.partypot.databinding.ActivityAddEventBinding
import com.example.partypot.ui.EventData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class addEvent : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityAddEventBinding
    private var selectedImageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance()
    private var map: GoogleMap? = null
    private var selectedLocation: LatLng? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.imageViewPhoto.setImageURI(selectedImageUri)
            }
        }

        binding.buttonSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.buttonSavePost.setOnClickListener {
            savePost()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {

            map?.isMyLocationEnabled = true
        }

        map?.setOnMapClickListener { latLng ->
            selectedLocation = latLng
            map?.clear()
            map?.addMarker(MarkerOptions().position(latLng).title("Event Location"))
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)  // Call to superclass implementation

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    map?.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(this, "Location permission is required to mark event location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePost() {
        val event = binding.editTextName.text.toString().trim()
        val condition = binding.editTextFacilities.text.toString().trim()
        val phone = binding.editTextContactNo.text.toString().trim()
        val price = binding.editTextRent.text.toString().trim()

        if (selectedImageUri == null || selectedLocation == null) {
            Toast.makeText(this, "Please select an image and mark a location", Toast.LENGTH_SHORT).show()
            return
        }

        if (event.isEmpty() || condition.isEmpty() || phone.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val postId = db.child("users").child(userId).child("events").push().key!!
        val storageRef = storage.reference.child("event_images/$postId.jpg")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val eventData = EventData(
                        event = event,
                        condition = condition,
                        price = price,
                        phone = phone,
                        photoDownloadUrl = uri.toString(),
                        latitude = selectedLocation?.latitude,
                        longitude = selectedLocation?.longitude
                    )

                    db.child("users").child(userId).child("events").child(postId)
                        .setValue(eventData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("AddEvent", "Failed to save event: ${e.message}")
                            Toast.makeText(this, "Failed to save event: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddEvent", "Failed to upload image: ${e.message}")
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
