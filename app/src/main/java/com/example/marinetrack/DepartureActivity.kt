package com.example.marinetrack

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DepartureActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var spinner: Spinner
    private val fishermenList = mutableListOf<String>()
    private var nicOfLoggedInUser = "2002"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_departure)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, BoatDetailsActivity::class.java))
            finish()
        }

        db = FirebaseFirestore.getInstance()

        val boatId = findViewById<TextInputEditText>(R.id.boatid)
        val departureDate = findViewById<EditText>(R.id.departureDate)
        val departureTime = findViewById<EditText>(R.id.departureTime)
        val arrivalDate = findViewById<EditText>(R.id.arrivalDate)
        val arrivalTime = findViewById<EditText>(R.id.arrivalTime)
        val numFishermen = findViewById<EditText>(R.id.numFishermen)
        spinner = findViewById(R.id.fishermenSpinner)
        val registerBtn = findViewById<Button>(R.id.Register)


        loadFishermenForUser(nicOfLoggedInUser)

        // Date Pickers
        val dateListener = { editText: EditText ->
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                editText.setText("$day/${month + 1}/$year")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        departureDate.setOnClickListener { dateListener(departureDate) }
        arrivalDate.setOnClickListener { dateListener(arrivalDate) }


        val timeListener = { editText: EditText ->
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                editText.setText(String.format("%02d:%02d", hour, minute))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        departureTime.setOnClickListener { timeListener(departureTime) }
        arrivalTime.setOnClickListener { timeListener(arrivalTime) }


        registerBtn.setOnClickListener {
            val boatIdValue = boatId.text.toString().trim()
            val depDateValue = departureDate.text.toString().trim()
            val depTimeValue = departureTime.text.toString().trim()
            val arrDateValue = arrivalDate.text.toString().trim()
            val arrTimeValue = arrivalTime.text.toString().trim()
            val numFishermenValue = numFishermen.text.toString().trim()
            val selectedFisherman = spinner.selectedItem?.toString() ?: "Unknown"

            if (boatIdValue.isEmpty() || depDateValue.isEmpty() || depTimeValue.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val departureData = hashMapOf(
                "boatId" to boatIdValue,
                "departureDate" to depDateValue,
                "departureTime" to depTimeValue,
                "arrivalDate" to arrDateValue,
                "arrivalTime" to arrTimeValue,
                "numberOfFishermen" to numFishermenValue,
                "fisherman" to selectedFisherman,
                "status" to "Active",
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("departures")
                .add(departureData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Departure added successfully!", Toast.LENGTH_SHORT).show()
                    clearFields()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun loadFishermenForUser(nic: String) {
        db.collection("fishermen")
            .whereEqualTo("nic", nic)
            .get()
            .addOnSuccessListener { result ->
                fishermenList.clear()
                for (document in result) {
                    val name = document.getString("name")
                    if (name != null) {
                        fishermenList.add(name)
                    }
                }

                if (fishermenList.isEmpty()) {
                    fishermenList.add("No fishermen found")
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fishermenList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading fishermen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        findViewById<TextInputEditText>(R.id.boatid).text?.clear()
        findViewById<EditText>(R.id.departureDate).text.clear()
        findViewById<EditText>(R.id.departureTime).text.clear()
        findViewById<EditText>(R.id.arrivalDate).text.clear()
        findViewById<EditText>(R.id.arrivalTime).text.clear()
        findViewById<EditText>(R.id.numFishermen).text.clear()
    }
}