package com.example.marinetrack

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DepartureActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var userNIC: String? = null
    private lateinit var fishermenMultiSelect: TextView
    private lateinit var selectedFishermenNames: MutableList<String>
    private var fishermenNames: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_departure)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        db = FirebaseFirestore.getInstance()

        userNIC = intent.getStringExtra("user_nic")
        if (userNIC == null) {
            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            userNIC = sharedPref.getString("user_nic", null)
        }

        val boatIdField = findViewById<TextInputEditText>(R.id.boatid)
        val depDateField = findViewById<EditText>(R.id.departureDate)
        val depTimeField = findViewById<EditText>(R.id.departureTime)
        val arrDateField = findViewById<EditText>(R.id.arrivalDate)
        val arrTimeField = findViewById<EditText>(R.id.arrivalTime)
        val numFishermenField = findViewById<EditText>(R.id.numFishermen)
        val registerButton = findViewById<Button>(R.id.Register)

        fishermenMultiSelect = findViewById(R.id.fishermenMultiSelect)
        selectedFishermenNames = mutableListOf()

        // Load fishermen under this userNIC
        loadFishermenNames()

        fishermenMultiSelect.setOnClickListener {
            showFishermanMultiSelectDialog()
        }

        depDateField.setOnClickListener { showDatePicker(depDateField) }
        arrDateField.setOnClickListener { showDatePicker(arrDateField) }
        depTimeField.setOnClickListener { showTimePicker(depTimeField) }
        arrTimeField.setOnClickListener { showTimePicker(arrTimeField) }

        registerButton.setOnClickListener {
            registerDeparture(
                boatIdField.text.toString(),
                depDateField.text.toString(),
                depTimeField.text.toString(),
                arrDateField.text.toString(),
                arrTimeField.text.toString(),
                numFishermenField.text.toString()
            )
        }
    }

    private fun loadFishermenNames() {
        userNIC?.let { nic ->
            db.collection("fishermen")
                .whereEqualTo("userNIC", nic)
                .get()
                .addOnSuccessListener { documents ->
                    fishermenNames.clear()
                    for (document in documents) {
                        val name = document.getString("name")
                        name?.let { fishermenNames.add(it) }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load fishermen", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showFishermanMultiSelectDialog() {
        if (fishermenNames.isEmpty()) {
            Toast.makeText(this, "No fishermen found", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedItems = BooleanArray(fishermenNames.size) { false }

        AlertDialog.Builder(this)
            .setTitle("Select Fishermen")
            .setMultiChoiceItems(fishermenNames.toTypedArray(), selectedItems) { _, which, isChecked ->
                val name = fishermenNames[which]
                if (isChecked) {
                    if (!selectedFishermenNames.contains(name)) selectedFishermenNames.add(name)
                } else {
                    selectedFishermenNames.remove(name)
                }
            }
            .setPositiveButton("Done") { _, _ ->
                fishermenMultiSelect.text = selectedFishermenNames.joinToString(", ")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(editText: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this, { _, year, month, day ->
                editText.setText("$day/${month + 1}/$year")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(editText: EditText) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this, { _, hour, minute ->
                editText.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
        ).show()
    }

    private fun registerDeparture(
        boatId: String, depDate: String, depTime: String, arrDate: String,
        arrTime: String, numFishermen: String
    ) {
        if (boatId.isEmpty() || depDate.isEmpty()) {
            Toast.makeText(this, "Please fill required fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val departureData = hashMapOf(
            "boatId" to boatId,
            "departureDate" to depDate,
            "departureTime" to depTime,
            "arrivalDate" to arrDate,
            "arrivalTime" to arrTime,
            "numberOfFishermen" to numFishermen,
            "selectedFishermen" to selectedFishermenNames
        )

        db.collection("departures")
            .add(departureData)
            .addOnSuccessListener {
                Toast.makeText(this, "Departure registered!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
