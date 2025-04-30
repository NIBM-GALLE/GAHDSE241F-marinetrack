package com.example.marinetrack
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ViewDepartureActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DepartureAdapter
    private lateinit var departureList: List<Departure>

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

        // Sample data (replace with real data from DB or Firebase)
        departureList = listOf(
            Departure("Boat123", "2025/02/25", "2025/05/25", "Active", "John, Alex"),
            Departure("Boat456", "2025/01/10", "2025/04/20", "Inactive", "Maria, Steve"),
            Departure("Boat789", "2025/03/01", "2025/06/01", "Pending", "Noah, Emma")
        )

        adapter = DepartureAdapter(departureList)
        recyclerView.adapter = adapter
    }
}
