package com.example.partypot

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.partypot.databinding.ActivityEventpageBinding
import com.example.partypot.ui.EventAdapter
import com.example.partypot.ui.EventData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Eventpage : AppCompatActivity() {
    private lateinit var binding: ActivityEventpageBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var eventsAdapter: EventAdapter
    private val events = mutableListOf<EventData>()
    private val allEvents = mutableListOf<EventData>()
    private val userEvents = mutableListOf<EventData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        eventsAdapter = EventAdapter(events)
        binding.recyclerView.adapter = eventsAdapter

        binding.post.setOnClickListener {
            val intent = Intent(this, FirstPage::class.java)
            startActivity(intent)
        }

        binding.AddEvent.setOnClickListener {
            val intent = Intent(this, addEvent::class.java)
            startActivity(intent)
        }
        binding.YourEvent.setOnClickListener {
            val intent = Intent(this, YourEvent::class.java)
            startActivity(intent)
        }



        loadEvent()
    }

    private fun loadEvent() {
        val currentUserUid = firebaseAuth.currentUser?.uid

        val usersRef = firebaseDatabase.reference.child("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allEvents.clear()
                userEvents.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key
                    userId?.let {
                        val eventsRef = userSnapshot.child("events")
                        for (postSnapshot in eventsRef.children) {
                            val event = postSnapshot.getValue(EventData::class.java)
                            event?.let {
                                allEvents.add(it)
                                if (userId == currentUserUid) {
                                    userEvents.add(it)
                                }
                            }
                        }
                    }
                }
                showAllEvents()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun showAllEvents() {
        events.clear()
        events.addAll(allEvents)
        eventsAdapter.notifyDataSetChanged()
    }



    private fun filterEvents(query: String?) {
        val filteredEvent = if (TextUtils.isEmpty(query)) {
            events
        } else {
            val filteredQuery = query.orEmpty()
            events.filter {
                (it.event?.contains(filteredQuery, true) == true) ||
                        (it.condition?.contains(filteredQuery, true) == true) ||
                        (it.location?.contains(filteredQuery, true) == true)
            }
        }
        eventsAdapter.updateList(filteredEvent)
    }
}


