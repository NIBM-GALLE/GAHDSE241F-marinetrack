package com.example.marinetrack

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.marinetrack.databinding.BoatCardBinding

class BoatAdapter(private val boatList: List<Boat>) :
    RecyclerView.Adapter<BoatAdapter.BoatViewHolder>() {

    inner class BoatViewHolder(val binding: BoatCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoatViewHolder {
        val binding = BoatCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoatViewHolder, position: Int) {
        val boat = boatList[position]
        with(holder.binding) {
            tvBoatID.text = "Boat ID: ${boat.boatId}"
            tvBoatName.text = "Name: ${boat.boatName}"
            tvBoatCapacity.text = "Capacity: ${boat.capacity} People"
            tvBoatStatus.text = "Status: ${boat.status}"
            validdate.text = "Registered On: ${boat.date}" // ✅ Show the date

            btnViewDetails.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, BoatDetailsActivity::class.java).apply {
                    putExtra("boatId", boat.boatId)
                    putExtra("boatName", boat.boatName)
                    putExtra("capacity", boat.capacity)
                    putExtra("status", boat.status)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = boatList.size
}
