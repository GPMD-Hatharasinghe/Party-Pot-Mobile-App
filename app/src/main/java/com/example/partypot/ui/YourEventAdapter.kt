package com.example.partypot.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.partypot.YourEvent
import com.example.partypot.databinding.YourplaceBinding
import com.squareup.picasso.Picasso

data class YourEventAdapter(
    private var events: List<EventData>,
    private var onPostLongclick: (EventData) -> Unit,
    ) : RecyclerView.Adapter<YourEventAdapter.PostViewHolder>(){

    inner class PostViewHolder(private val binding: YourplaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event:EventData) {
            binding.textViewname.text = event.event
            binding.textViewContact.text = event.phone
            binding.textViewFacilities.text = event.condition
            binding.textViewRent.text = event.price

            Picasso.get().load(event.photoDownloadUrl).into(binding.imageViewPhoto)


            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, YourEvent::class.java)
                intent.putExtra("place", event.location)
                context.startActivity(intent)
            }

            binding.root.setOnLongClickListener{
                onPostLongclick(event)
                true

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = YourplaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun updateList(newEvents: List<EventData>) {
        events = newEvents
        notifyDataSetChanged()
    }
}

