package com.example.marinetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registerpage)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        db = FirebaseFirestore.getInstance()

        val registerButton: Button = findViewById(R.id.Register)
        val nameEditText: EditText = findViewById(R.id.name)
        val nicEditText: EditText = findViewById(R.id.editTextTextNIC)
        val contactEditText: EditText = findViewById(R.id.editTextTextContactNumber)
        val emailEditText: EditText = findViewById(R.id.editTextTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextTextPassword)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val nic = nicEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (name.isEmpty() || nic.isEmpty() || contact.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nic.length != 12 || !nic.all { it.isDigit() }) {
                Toast.makeText(this, "NIC must be exactly 12 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (contact.length != 10 || !contact.all { it.isDigit() }) {
                Toast.makeText(this, "Contact number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.contains("@")) {
                Toast.makeText(this, "Invalid email: ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = hashMapOf(
                "name" to name,
                "nic" to nic,
                "contact" to contact,
                "email" to email,
                "password" to password
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
