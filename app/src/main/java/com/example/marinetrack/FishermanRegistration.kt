package com.example.marinetrack

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class FishermanRegistration : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etId: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText
    private lateinit var etAge: EditText
    private lateinit var imgIdPhoto: ImageView
    private lateinit var imgBoatLicense: ImageView
    private lateinit var btnSave: Button

    private var idPhotoUri: Uri? = null
    private var boatLicenseUri: Uri? = null

    private val PICK_ID_PHOTO_REQUEST = 1
    private val PICK_BOAT_LICENSE_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fishermanregistration)

        etName = findViewById(R.id.etName)
        etId = findViewById(R.id.etId)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etAge = findViewById(R.id.etAge)
        imgIdPhoto = findViewById(R.id.imgIdPhoto)
        imgBoatLicense = findViewById(R.id.imgBoatLicense)
        btnSave = findViewById(R.id.btnSave)


        imgIdPhoto.setOnClickListener {
            pickImage(PICK_ID_PHOTO_REQUEST)
        }


        imgBoatLicense.setOnClickListener {
            pickImage(PICK_BOAT_LICENSE_REQUEST)
        }


        btnSave.setOnClickListener {
            saveFishermanData()
        }
    }

    private fun pickImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when (requestCode) {
                PICK_ID_PHOTO_REQUEST -> {
                    idPhotoUri = data.data
                    imgIdPhoto.setImageURI(idPhotoUri)
                }
                PICK_BOAT_LICENSE_REQUEST -> {
                    boatLicenseUri = data.data
                    imgBoatLicense.setImageURI(boatLicenseUri)
                }
            }
        }
    }

    private fun saveFishermanData() {
        val name = etName.text.toString().trim()
        val id = etId.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val age = etAge.text.toString().trim()

        if (name.isEmpty() || id.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty() || age.isEmpty() || idPhotoUri == null || boatLicenseUri == null) {
            Toast.makeText(this, "Please fill all fields and upload images", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("Fishermen")
        val fishermanId = databaseReference.push().key ?: return

        uploadImage(idPhotoUri!!) { idPhotoUrl ->
            uploadImage(boatLicenseUri!!) { boatLicenseUrl ->
                val fisherman = Fisherman(fishermanId, name, id, phone, email, address, age, idPhotoUrl, boatLicenseUrl)
                databaseReference.child(fishermanId).setValue(fisherman)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun uploadImage(imageUri: Uri, callback: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().getReference("FishermenImages/${UUID.randomUUID()}")
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }
}

data class Fisherman(
    val fishermanId: String,
    val name: String,
    val id: String,
    val phone: String,
    val email: String,
    val address: String,
    val age: String,
    val idPhotoUrl: String,
    val boatLicenseUrl: String
)
