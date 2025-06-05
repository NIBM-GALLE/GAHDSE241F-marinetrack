package com.example.marinetrack


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar



class BoatDetailsActivity : AppCompatActivity() {
    private var userNIC: String? = null
    private var boatId: String? = null //
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

        userNIC = intent.getStringExtra("user_nic")
        boatId = intent.getStringExtra("boatId")

        // Option 2: Get NIC from SharedPreferences as fallback
        if (userNIC == null) {
            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            userNIC = sharedPref.getString("user_nic", null)
        }



        val addButton: Button = findViewById(R.id.adddeparture)
        addButton.setOnClickListener {
            val intent = Intent(this, DepartureActivity::class.java)
            intent.putExtra("id", boatId)
            startActivity(intent)

        }

        val viewButton: Button = findViewById(R.id.viewdepartures)
        viewButton.setOnClickListener {
            val intent = Intent(this, ViewDepartureActivity::class.java)
            intent.putExtra("id", boatId)
            startActivity(intent)
        }
    }
}