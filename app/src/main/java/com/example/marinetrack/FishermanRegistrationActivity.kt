package com.example.marinetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore

class FishermanRegistrationActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fishermanregistration)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        db = FirebaseFirestore.getInstance()

        val registerButton: Button = findViewById(R.id.Register)
        val nicEditText: EditText = findViewById(R.id.editTextTextNIC)
        val boatIdEditText: EditText = findViewById(R.id.boatid)
        val fishermenNameEditText: EditText = findViewById(R.id.fishermenname)
        val fishermenNICEditText: EditText = findViewById(R.id.fishermenNIC)
        val contactEditText: EditText = findViewById(R.id.editTextTextContactNumber)
        val addressEditText: EditText = findViewById(R.id.address)
        val emailEditText: EditText = findViewById(R.id.editTextTextEmail)


        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userNic = sharedPref.getString("user_nic", "") ?: ""
        nicEditText.setText(userNic)

        registerButton.setOnClickListener {
            val nic = nicEditText.text.toString().trim()
            val boatId = boatIdEditText.text.toString().trim()
            val fishermenName = fishermenNameEditText.text.toString().trim()
            val fishermenNIC = fishermenNICEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (nic.isEmpty() || boatId.isEmpty() || fishermenName.isEmpty() || fishermenNIC.isEmpty() || contact.isEmpty() || address.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fishermenData = hashMapOf(
                "nic" to nic,
                "boatId" to boatId,
                "fishermenName" to fishermenName,
                "fishermenNIC" to fishermenNIC,
                "contact" to contact,
                "address" to address,
                "email" to email
            )

            db.collection("fishermen")
                .add(fishermenData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Fisherman Registered Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
