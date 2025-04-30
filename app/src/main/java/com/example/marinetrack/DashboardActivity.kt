package com.example.marinetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val nic = sharedPref.getString("user_nic", null)
        val email = sharedPref.getString("user_email", "Unknown User")


        val usernameTextView: TextView = findViewById(R.id.username)
        usernameTextView.text = "Welcome, $email"


        val logoutButton: ImageView = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            sharedPref.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        val boatButton: Button = findViewById(R.id.boatregister)
        boatButton.setOnClickListener {
            val intent = Intent(this, BoatRegisterActivity::class.java)
            startActivity(intent)
        }


        val fishermenButton: Button = findViewById(R.id.fishermenregister)
        fishermenButton.setOnClickListener {
            val intent = Intent(this, FishermanRegistrationActivity::class.java)
            startActivity(intent)
        }


        val showdetailsButton: Button = findViewById(R.id.display)
        showdetailsButton.setOnClickListener {
            val intent = Intent(this, BoatListActivity::class.java)
            startActivity(intent)
        }
    }
}