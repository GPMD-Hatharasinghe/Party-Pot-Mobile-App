package com.example.partypot.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.partypot.databinding.ItemPostBinding
import com.example.partypot.eventPlace
import com.squareup.picasso.Picasso

class EventAdapter(private var events: List<EventData>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: EventData) {
            binding.textViewTitle.text = event.event

            Picasso.get().load(event.photoDownloadUrl).into(binding.imageViewPhoto)


            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, eventPlace::class.java)
                intent.putExtra("events", event.location)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun updateList(newEvents: List<EventData>) {
        events= newEvents
        notifyDataSetChanged()
    }
}
