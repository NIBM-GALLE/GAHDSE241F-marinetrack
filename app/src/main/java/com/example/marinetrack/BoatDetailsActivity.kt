package com.example.marinetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class BoatDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boatdetails)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        val addButton: Button = findViewById(R.id.adddeparture)
        addButton.setOnClickListener {
            val intent = Intent(this, DepartureActivity::class.java)
            startActivity(intent)
        }

        val viewButton: Button = findViewById(R.id.viewdepartures)
        viewButton.setOnClickListener {
            val intent = Intent(this, ViewDepartureActivity::class.java)
            startActivity(intent)
        }
    }
}