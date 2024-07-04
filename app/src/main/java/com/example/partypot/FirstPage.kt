package com.example.partypot

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.partypot.databinding.ActivityFirstPageBinding
import com.example.partypot.ui.PostAdapter
import com.example.partypot.ui.PostData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FirstPage : AppCompatActivity() {
    private lateinit var binding: ActivityFirstPageBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var postAdapter: PostAdapter
    private val posts = mutableListOf<PostData>()
    private val allPosts = mutableListOf<PostData>()
    private val userPosts = mutableListOf<PostData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        postAdapter = PostAdapter(posts)
        binding.recyclerView.adapter = postAdapter

        binding.event.setOnClickListener {
            val intent = Intent(this, Eventpage::class.java)
            startActivity(intent)
        }
        binding.YourPlace.setOnClickListener {
            val intent = Intent(this, YourPlace::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val intent = Intent(this, addPartypot::class.java)
            startActivity(intent)
        }



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
                        val postsRef = userSnapshot.child("posts")
                        for (postSnapshot in postsRef.children) {
                            val post = postSnapshot.getValue(PostData::class.java)
                            post?.let {
                                allPosts.add(it)
                                if (userId == currentUserUid) {
                                    userPosts.add(it)
                                }
                            }
                        }
                    }
                }
                showAllPosts()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun showAllPosts() {
        posts.clear()
        posts.addAll(allPosts)
        postAdapter.notifyDataSetChanged()
    }


    private fun filterPosts(query: String?) {
        val filteredPosts = if (TextUtils.isEmpty(query)) {
            posts
        } else {
            val filteredQuery = query.orEmpty()
            posts.filter {
                (it.name?.contains(filteredQuery, true) == true) ||
                        (it.facilities?.contains(filteredQuery, true) == true) ||
                        (it.place?.contains(filteredQuery, true) == true)
            }
        }
        postAdapter.updateList(filteredPosts)
    }
}
