package com.example.partypot

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.partypot.databinding.ActivityYourPlaceBinding
import com.example.partypot.ui.PostData
import com.example.partypot.ui.YourPostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class YourPlace : AppCompatActivity() {


    private lateinit var binding: ActivityYourPlaceBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var postAdapter: YourPostAdapter
    private val posts = mutableListOf<PostData>()
    private val allPosts = mutableListOf<PostData>()
    private val userPosts = mutableListOf<PostData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        postAdapter = YourPostAdapter(posts,this::onPostLongClick)
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

    private fun onPostLongClick(post: PostData) {
        val currentUserUid = firebaseAuth.currentUser?.uid

        if (currentUserUid != null) {
            val userRef = firebaseDatabase.reference.child("users").child(currentUserUid).child("posts")

            AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                    userRef.orderByChild("photoDownloadUrl").equalTo(post.photoDownloadUrl)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (postSnapshot in snapshot.children) {
                                    postSnapshot.ref.removeValue()
                                        .addOnSuccessListener {
                                            Toast.makeText(this@YourPlace, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                                            loadPosts()
                                        }
                                        .addOnFailureListener { _ ->
                                            Toast.makeText(this@YourPlace, "Failed to delete post", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@YourPlace, "Failed to delete post", Toast.LENGTH_SHORT).show()
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
                (it.name?.contains(filteredQuery, true) == true) ||
                        (it.facilities?.contains(filteredQuery, true) == true) ||
                        (it.place?.contains(filteredQuery, true) == true)
            }
        }
        postAdapter.updateList(filteredPosts)
    }
}

