package com.example.partypot.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.partypot.Placepage
import com.example.partypot.databinding.ItemPostBinding
import com.squareup.picasso.Picasso

class PostAdapter(private var posts: List<PostData>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostData) {
            binding.textViewTitle.text = post.name

            Picasso.get().load(post.photoDownloadUrl).into(binding.imageViewPhoto)


            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, Placepage::class.java)
                intent.putExtra("place", post.place)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun updateList(newPosts: List<PostData>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
