package com.example.marinetrack

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class BoatRegisterActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null
    private var documentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boatregistration)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setMessage("Registering boat...")
            setCancelable(false)
        }

        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userNic = sharedPref.getString("user_nic", "") ?: ""
        val userEmail = sharedPref.getString("user_email", "") ?: ""

        val registerButton: Button = findViewById(R.id.Register)
        val uploadImageButton: Button = findViewById(R.id.uploadImageButton)
        val uploadDocumentButton: Button = findViewById(R.id.uploadDocumentButton)

        val nameEditText: EditText = findViewById(R.id.name)
        val nicEditText: EditText = findViewById(R.id.editTextTextNIC)
        val contactEditText: EditText = findViewById(R.id.editTextTextContactNumber)
        val emailEditText: EditText = findViewById(R.id.editTextTextEmail)
        val addressEditText: EditText = findViewById(R.id.address)
        val boatNameEditText: EditText = findViewById(R.id.boatname)
        val serialNumberEditText: EditText = findViewById(R.id.serialnumber)
        val boatLengthEditText: EditText = findViewById(R.id.boatlength)
        val yearEditText: EditText = findViewById(R.id.year)
        val powerEditText: EditText = findViewById(R.id.power)
        val capacityEditText: EditText = findViewById(R.id.capacity)

        nicEditText.setText(userNic)
        emailEditText.setText(userEmail)

        // Image Picker
        val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show()
        }

        uploadImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Document Picker
        val documentPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            documentUri = uri
            Toast.makeText(this, "Document selected successfully", Toast.LENGTH_SHORT).show()
        }

        uploadDocumentButton.setOnClickListener {
            documentPicker.launch("application/pdf")
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val nic = nicEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val boatName = boatNameEditText.text.toString().trim()
            val serialNumber = serialNumberEditText.text.toString().trim()
            val boatLength = boatLengthEditText.text.toString().trim()
            val year = yearEditText.text.toString().trim()
            val power = powerEditText.text.toString().trim()
            val capacity = capacityEditText.text.toString().trim()

            if (imageUri == null || documentUri == null ||
                name.isEmpty() || nic.isEmpty() || contact.isEmpty() || email.isEmpty() || address.isEmpty() ||
                boatName.isEmpty() || serialNumber.isEmpty() || boatLength.isEmpty() || year.isEmpty() ||
                power.isEmpty() || capacity.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields and select both an image and a document", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerButton.isEnabled = false
            progressDialog.show()

            val imageRef = storage.reference.child("uploads/images/${UUID.randomUUID()}.jpg")
            val documentRef = storage.reference.child("uploads/documents/${UUID.randomUUID()}.pdf")

            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        documentRef.putFile(documentUri!!)
                            .addOnSuccessListener {
                                documentRef.downloadUrl.addOnSuccessListener { documentUrl ->
                                    val boat = hashMapOf(
                                        "name" to name,
                                        "nic" to nic,
                                        "contact" to contact,
                                        "email" to email,
                                        "address" to address,
                                        "boatName" to boatName,
                                        "serialNumber" to serialNumber,
                                        "boatLength" to boatLength,
                                        "year" to year,
                                        "power" to power,
                                        "capacity" to capacity,
                                        "imageUrl" to imageUrl.toString(),
                                        "documentUrl" to documentUrl.toString()
                                    )

                                    db.collection("boat")
                                        .add(boat)
                                        .addOnSuccessListener {
                                            progressDialog.dismiss()
                                            Toast.makeText(this, "Boat Registered Successfully", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, DashboardActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { firestoreError ->
                                            progressDialog.dismiss()
                                            registerButton.isEnabled = true
                                            Log.e("FirestoreError", firestoreError.message.toString())
                                            Toast.makeText(this, "Error saving to database: ${firestoreError.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { docErr ->
                                progressDialog.dismiss()
                                registerButton.isEnabled = true
                                Log.e("DocumentUploadError", docErr.message.toString())
                                Toast.makeText(this, "Failed to upload document: ${docErr.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { imgErr ->
                    progressDialog.dismiss()
                    registerButton.isEnabled = true
                    Log.e("ImageUploadError", imgErr.message.toString())
                    Toast.makeText(this, "Failed to upload image: ${imgErr.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
