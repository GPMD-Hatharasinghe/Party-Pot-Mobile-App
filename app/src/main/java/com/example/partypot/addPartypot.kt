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
import com.example.partypot.databinding.ActivityAddPartypotBinding
import com.example.partypot.ui.PostData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class addPartypot : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var binding: ActivityAddPartypotBinding
    private var selectedLocation: LatLng? = null
    private var selectedImageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance()
    private var map: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPartypotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
            map?.addMarker(MarkerOptions().position(latLng).title("Place Location"))
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    private fun savePost() {
        val name = binding.editTextName.text.toString().trim()
        val facilities = binding.editTextFacilities.text.toString().trim()
        val rent = binding.editTextRent.text.toString().trim()
        val phone = binding.editTextContactNo.text.toString().trim()

        if ( selectedImageUri == null) {
            Toast.makeText(this, "Please select a location and an image", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty() || facilities.isEmpty() || rent.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val postId = db.child("users").child(userId).child("posts").push().key!!
        val storageRef = storage.reference.child("post_images/$postId.jpg")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val post = PostData(
                        name = name,
                        facilities = facilities,
                        place = "${selectedLocation!!.latitude},${selectedLocation!!.longitude}",
                        rent = rent,
                        phone = phone,
                        photoDownloadUrl = uri.toString(),
                        latitude = selectedLocation!!.latitude,
                        longitude = selectedLocation!!.longitude
                    )

                    db.child("users").child(userId).child("posts").child(postId)
                        .setValue(post)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Post saved successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("AddPartypot", "Failed to save post: ${e.message}")
                            Toast.makeText(this, "Failed to save post: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddPartypot", "Failed to upload image: ${e.message}")
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
