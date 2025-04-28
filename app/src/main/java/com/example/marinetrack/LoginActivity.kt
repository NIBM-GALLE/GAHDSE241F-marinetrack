package com.example.marinetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginpage)

        firestore = FirebaseFirestore.getInstance()

        val emailEditText: EditText = findViewById(R.id.editTextTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerTextView: TextView = findViewById(R.id.textViewRegister)

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val usersCollection = firestore.collection("users")

        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val storedPassword = document.getString("password")
                        if (storedPassword == password) {
                            val storedNIC = document.getString("nic")
                            val storeEmail = document.getString("email")

                            // Save to SharedPreferences
                            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("user_nic", storedNIC)
                                putString("user_email", storeEmail)
                                apply()
                            }

                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                            return@addOnSuccessListener
                        }
                    }
                    Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching data: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
