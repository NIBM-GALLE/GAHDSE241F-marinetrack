package com.example.marinetrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DepartureAdapter(private val departures: List<Departure>) :
    RecyclerView.Adapter<DepartureAdapter.DepartureViewHolder>() {

    inner class DepartureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val boatId: TextView = itemView.findViewById(R.id.BoatId)
        val departureDate: TextView = itemView.findViewById(R.id.date)
        val arrivalDate: TextView = itemView.findViewById(R.id.arrivaldate)
        val status: TextView = itemView.findViewById(R.id.tvBoatStatus)
        val fisherman: TextView = itemView.findViewById(R.id.tvFisherman)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.departure_card, parent, false)
        return DepartureViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepartureViewHolder, position: Int) {
        val departure = departures[position]
        holder.boatId.text = "Boat ID: ${departure.boatId}"
        holder.departureDate.text = "Departure Date: ${departure.departureDate}"
        holder.arrivalDate.text = "Arrival Date: ${departure.arrivalDate}"
        holder.status.text = "Status: ${departure.status}"
        holder.fisherman.text = "Fisherman: ${departure.fishermen}"
    }

    override fun getItemCount(): Int = departures.size
}