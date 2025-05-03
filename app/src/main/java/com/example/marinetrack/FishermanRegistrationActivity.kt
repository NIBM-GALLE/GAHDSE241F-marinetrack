package com.example.marinetrack

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class FishermanRegistrationActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null
    private var documentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fishermanregistration)

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
            setMessage("Registering fisherman...")
            setCancelable(false)
        }

        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userNic = sharedPref.getString("user_nic", "") ?: ""
        val userEmail = sharedPref.getString("user_email", "") ?: ""

        val registerButton: Button = findViewById(R.id.Register)
        val uploadImageButton: Button = findViewById(R.id.btnAddImage)
        val uploadDocumentButton: Button = findViewById(R.id.btnUploadDocument)

        val nameEditText: EditText = findViewById(R.id.fishermenname)
        val nicEditText: EditText = findViewById(R.id.fishermenNIC)
        val contactEditText: EditText = findViewById(R.id.editTextTextContactNumber)
        val emailEditText: EditText = findViewById(R.id.editTextTextEmail)
        val addressEditText: EditText = findViewById(R.id.address)
        val boatIdEditText: EditText = findViewById(R.id.boatid)

        nicEditText.setText(userNic)
        emailEditText.setText(userEmail)

        val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show()
        }

        uploadImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

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
            val boatId = boatIdEditText.text.toString().trim()

            if (imageUri == null || documentUri == null ||
                name.isEmpty() || nic.isEmpty() || contact.isEmpty() || email.isEmpty() ||
                address.isEmpty() || boatId.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields and select both an image and a document", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerButton.isEnabled = false
            progressDialog.show()

            val imageRef = storage.reference.child("uploads/fisherman_images/${UUID.randomUUID()}.jpg")
            val documentRef = storage.reference.child("uploads/fisherman_documents/${UUID.randomUUID()}.pdf")

            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        documentRef.putFile(documentUri!!)
                            .addOnSuccessListener {
                                documentRef.downloadUrl.addOnSuccessListener { documentUrl ->
                                    val fisherman = hashMapOf(
                                        "fishermenName" to name,
                                        "fishermenNIC" to nic,
                                        "contact" to contact,
                                        "email" to email,
                                        "address" to address,
                                        "boatId" to boatId,
                                        "imageUrl" to imageUrl.toString(),
                                        "documentUrl" to documentUrl.toString()
                                    )

                                    db.collection("fishermen")
                                        .add(fisherman)
                                        .addOnSuccessListener {
                                            progressDialog.dismiss()
                                            Toast.makeText(this, "Fisherman Registered Successfully", Toast.LENGTH_SHORT).show()
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
