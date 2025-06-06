package com.example.marinetrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ViewDepartureActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DepartureAdapter
    private var departureList = mutableListOf<Departure>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewdeparture)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, BoatDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewDepartures)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DepartureAdapter(departureList)
        recyclerView.adapter = adapter

        val boatId = intent.getStringExtra("id")
        if (boatId != null) {
            fetchDeparturesForBoat(boatId)
        }
    }

    private fun fetchDeparturesForBoat(boatId: String) {
        db.collection("departures")
            .whereEqualTo("boatRegistrationId", boatId)
            .get()
            .addOnSuccessListener { documents ->
                departureList.clear()

                for (doc in documents) {
                    //val departureDateStr = doc.getString("boatName") ?: ""
                    //val arrivalDate = doc.getString("destination") ?: ""
                    val departureTimestamp = doc.getTimestamp("departureTimestamp")?.toDate()
                    val estimatedReturnTimestamp = doc.getTimestamp("estimatedReturnTimestamp")?.toDate()

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val departureStr = departureTimestamp?.let { dateFormat.format(it) } ?: ""
                    val returnStr = estimatedReturnTimestamp?.let { dateFormat.format(it) } ?: ""

                    val fishermen = (doc.get("fishermenNames") as? List<*>)?.joinToString(", ") ?: ""

                    // Determine status based on departureDate
                    val status = doc.getString("status") ?: ""

                    val departure = Departure(
                        boatId = boatId,
                        departureDate = departureStr,
                        arrivalDate = returnStr,
                        status = status,
                        fishermen = fishermen
                    )
                    departureList.add(departure)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ViewDeparture", "Error fetching departures", e)
            }
    }
}