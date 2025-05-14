import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marinetrack.BoatDetailsActivity
import com.example.marinetrack.Departure
import com.example.marinetrack.DepartureAdapter
import com.example.marinetrack.R
import com.google.firebase.firestore.FirebaseFirestore


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
                    val departure = Departure(
                        boatId = doc.getString("boatId") ?: "",
                        departureDate = doc.getString("departureDate") ?: "",
                        arrivalDate = doc.getString("arrivalDate") ?: "",
                        status = "", // Add logic if you store status
                        fishermen = (doc.get("selectedFishermen") as? List<*>)?.joinToString(", ") ?: ""
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
