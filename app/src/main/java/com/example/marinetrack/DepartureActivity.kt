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
import java.text.SimpleDateFormat
import java.util.*

class DepartureActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var userNIC: String? = null
    private lateinit var fishermenMultiSelect: TextView
    private lateinit var selectedFishermenNames: MutableList<String>
    private lateinit var selectedFishermenNICs: MutableList<String>
    private var fishermenNames: MutableList<String> = mutableListOf()
    private var fishermenMap: MutableMap<String, String> = mutableMapOf() // name -> NIC

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

        val passedBoatId = intent.getStringExtra("id")
        passedBoatId?.let {
            boatIdField.setText(it)
        }

        fishermenMultiSelect = findViewById(R.id.fishermenMultiSelect)
        selectedFishermenNames = mutableListOf()
        selectedFishermenNICs = mutableListOf()

        loadFishermenNames()

        fishermenMultiSelect.setOnClickListener {
            showFishermanMultiSelectDialog()
        }

        depDateField.setOnClickListener { showDatePicker(depDateField) }
        arrDateField.setOnClickListener { showDatePicker(arrDateField) }
        depTimeField.setOnClickListener { showTimePicker(depTimeField) }
        arrTimeField.setOnClickListener { showTimePicker(arrTimeField) }

        registerButton.setOnClickListener {
            val boatId = boatIdField.text.toString()
            val depDate = depDateField.text.toString()
            val depTime = depTimeField.text.toString()
            val arrDate = arrDateField.text.toString()
            val arrTime = arrTimeField.text.toString()
            val numFishermen = numFishermenField.text.toString()

            if (boatId.isEmpty() || depDate.isEmpty()) {
                Toast.makeText(this, "Please fill required fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkFishermanAvailability(
                selectedFishermenNICs,
                depDate,
                depTime,
                arrDate,
                arrTime
            ) {
                saveDeparture(
                    boatId,
                    depDate,
                    depTime,
                    arrDate,
                    arrTime,
                    numFishermen
                )
            }
        }
    }

    private fun loadFishermenNames() {
        userNIC?.let { nic ->
            db.collection("fishermen")
                .whereEqualTo("ownerNIC", nic)
                .get()
                .addOnSuccessListener { documents ->
                    fishermenNames.clear()
                    fishermenMap.clear()
                    for (document in documents) {
                        val name = document.getString("fishermenName")
                        val nic = document.getString("fishermenNIC")
                        if (name != null && nic != null) {
                            fishermenNames.add(name)
                            fishermenMap[name] = nic
                        }
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
                val nic = fishermenMap[name]
                if (isChecked) {
                    if (!selectedFishermenNames.contains(name)) {
                        selectedFishermenNames.add(name)
                        nic?.let { selectedFishermenNICs.add(it) }
                    }
                } else {
                    selectedFishermenNames.remove(name)
                    nic?.let { selectedFishermenNICs.remove(it) }
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

    private fun checkFishermanAvailability(
        selectedNICs: List<String>,
        depDate: String,
        depTime: String,
        arrDate: String,
        arrTime: String,
        onSuccess: () -> Unit
    ) {
        db.collection("departures")
            .whereArrayContainsAny("selectedFishermen", selectedNICs)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val existingDepDate = doc.getString("departureDate") ?: continue
                    val existingDepTime = doc.getString("departureTime") ?: continue
                    val existingArrDate = doc.getString("arrivalDate") ?: continue
                    val existingArrTime = doc.getString("arrivalTime") ?: continue

                    if (datesOverlap(
                            depDate, depTime, arrDate, arrTime,
                            existingDepDate, existingDepTime, existingArrDate, existingArrTime
                        )
                    ) {
                        Toast.makeText(
                            this,
                            "Some fishermen are already assigned to another boat in this period!",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }
                }
                onSuccess() // No conflicts found
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check availability", Toast.LENGTH_SHORT).show()
            }
    }

    private fun datesOverlap(
        start1Date: String, start1Time: String,
        end1Date: String, end1Time: String,
        start2Date: String, start2Time: String,
        end2Date: String, end2Time: String
    ): Boolean {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val start1 = format.parse("$start1Date $start1Time")
            val end1 = format.parse("$end1Date $end1Time")
            val start2 = format.parse("$start2Date $start2Time")
            val end2 = format.parse("$end2Date $end2Time")

            start1 != null && end1 != null && start2 != null && end2 != null &&
                    start1.before(end2) && start2.before(end1)
        } catch (e: Exception) {
            false
        }
    }

    private fun saveDeparture(
        boatId: String, depDate: String, depTime: String,
        arrDate: String, arrTime: String, numFishermen: String
    ) {
        val departureData = hashMapOf(
            "boatId" to boatId,
            "departureDate" to depDate,
            "departureTime" to depTime,
            "arrivalDate" to arrDate,
            "arrivalTime" to arrTime,
            "numberOfFishermen" to numFishermen,
            "selectedFishermen" to selectedFishermenNICs
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
