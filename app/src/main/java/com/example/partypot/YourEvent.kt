package com.example.partypot

//import com.example.partypot.databinding.ActivityYourPlaceBinding
//import com.example.partypot.ui.PostData
//import com.example.partypot.ui.YourPostAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.partypot.databinding.ActivityYourEventBinding
import com.example.partypot.ui.EventData
import com.example.partypot.ui.YourEventAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class YourEvent : AppCompatActivity() {

    private lateinit var binding: ActivityYourEventBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var postAdapter: YourEventAdapter
    private val posts = mutableListOf<EventData>()
    private val allPosts = mutableListOf<EventData>()
    private val userPosts = mutableListOf<EventData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        postAdapter = YourEventAdapter(posts,this::onPostLongClick)
        binding.recyclerView.adapter = postAdapter


        loadPosts()
    }

    private fun loadPosts() {
        val currentUserUid = firebaseAuth.currentUser?.uid

        val usersRef = firebaseDatabase.reference.child("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allPosts.clear()
                userPosts.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key
                    userId?.let {
                        val postsRef = userSnapshot.child("events")
                        for (postSnapshot in postsRef.children) {
                            val post = postSnapshot.getValue(EventData::class.java)
                            post?.let {
                                allPosts.add(it)
                                if (userId == currentUserUid) {
                                    userPosts.add(it)
                                }
                            }
                        }
                    }
                }
                showUserPosts()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun showUserPosts() {
        posts.clear()
        posts.addAll(userPosts)
        postAdapter.notifyDataSetChanged()
    }

    private fun onPostLongClick(post: EventData) {
        val currentUserUid = firebaseAuth.currentUser?.uid

        if (currentUserUid != null) {
            val userRef = firebaseDatabase.reference.child("users").child(currentUserUid).child("events")

            AlertDialog.Builder(this)
                .setTitle("Delete event")
                .setMessage("Are you sure you want to delete this Event?")
                .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                    userRef.orderByChild("photoDownloadUrl").equalTo(post.photoDownloadUrl)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (postSnapshot in snapshot.children) {
                                    postSnapshot.ref.removeValue()
                                        .addOnSuccessListener {
                                            Toast.makeText(this@YourEvent, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                                            loadPosts()
                                        }
                                        .addOnFailureListener { _ ->
                                            Toast.makeText(this@YourEvent, "Failed to delete post", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@YourEvent, "Failed to delete post", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun filterPosts(query: String?) {
        val filteredPosts = if (TextUtils.isEmpty(query)) {
            posts
        } else {
            val filteredQuery = query.orEmpty()
            posts.filter {
                (it.event?.contains(filteredQuery, true) == true) ||
                        (it.condition?.contains(filteredQuery, true) == true) ||
                        (it.location?.contains(filteredQuery, true) == true)
            }
        }
        postAdapter.updateList(filteredPosts)
    }
}