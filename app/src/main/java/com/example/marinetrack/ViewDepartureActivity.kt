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
            .whereEqualTo("boatId", boatId)
            .get()
            .addOnSuccessListener { documents ->
                departureList.clear()

                for (doc in documents) {
                    val departureDateStr = doc.getString("departureDate") ?: ""
                    val arrivalDate = doc.getString("arrivalDate") ?: ""
                    val fishermen = (doc.get("selectedFishermen") as? List<*>)?.joinToString(", ") ?: ""

                    // Determine status based on departureDate
                    val status = if (departureDateStr.isEmpty()) {
                        "Pending"
                    } else {
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val departureDate = sdf.parse(departureDateStr)
                            val currentDate = sdf.parse(sdf.format(Date()))

                            if (departureDate != null && departureDate.after(currentDate)) {
                                "Pending"
                            } else {
                                "Active"
                            }
                        } catch (e: Exception) {
                            Log.e("ViewDeparture", "Date parsing error", e)
                            "Pending"
                        }
                    }

                    val departure = Departure(
                        boatId = boatId,
                        departureDate = departureDateStr,
                        arrivalDate = arrivalDate,
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
