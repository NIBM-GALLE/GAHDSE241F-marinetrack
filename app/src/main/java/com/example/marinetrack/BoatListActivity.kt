package com.example.marinetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marinetrack.databinding.ActivityBoatlistBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class BoatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoatlistBinding
    private lateinit var boatAdapter: BoatAdapter
    private val boatList = mutableListOf<Boat>()

    private lateinit var firestore: FirebaseFirestore
    private var userNIC: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoatlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Option 1: Get NIC from Intent
        userNIC = intent.getStringExtra("user_nic")

        // Option 2: Get NIC from SharedPreferences as fallback
        if (userNIC == null) {
            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            userNIC = sharedPref.getString("user_nic", null)
        }

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set up RecyclerView
        binding.recyclerViewBoats.layoutManager = LinearLayoutManager(this)
        boatAdapter = BoatAdapter(boatList)
        binding.recyclerViewBoats.adapter = boatAdapter

        // Fetch boats if NIC exists
        if (!userNIC.isNullOrEmpty()) {
            fetchBoatsForUser(userNIC!!)
        } else {
            Toast.makeText(this, "NIC not found! Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchBoatsForUser(nic: String) {
        firestore.collection("boat")
            .whereEqualTo("nic", userNIC)
            .get()
            .addOnSuccessListener { documents ->
                boatList.clear()
                for (document in documents) {
                    val id = document.getString("registrationId") ?: ""
                    val name = document.getString("boatName") ?: ""
                    val capacity = document.getString("capacity") ?: ""
                    val status = document.getString("status") ?: ""

                    // Get createdAt timestamp (if exists)
                    val timestamp = document.getTimestamp("createdAt")
                    val formattedDate = if (timestamp != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.format(timestamp.toDate())
                    } else {
                        "2025-05-30"
                    }

                    val boat = Boat(id, name, capacity, status, formattedDate)
                    boatList.add(boat)
                }
                boatAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch boats: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

}