package com.example.partypot.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.partypot.UpdatePlace
import com.example.partypot.databinding.YourplaceBinding
import com.squareup.picasso.Picasso

data class YourPostAdapter(
    private var posts: List<PostData>,
    private var onPostLongclick: (PostData) -> Unit,

) : RecyclerView.Adapter<YourPostAdapter.PostViewHolder>(){

inner class PostViewHolder(private val binding: YourplaceBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: PostData) {
        binding.textViewname.text = post.name
        binding.textViewContact.text = post.phone
        binding.textViewFacilities.text = post.facilities
        binding.textViewRent.text = post.rent

        Picasso.get().load(post.photoDownloadUrl).into(binding.imageViewPhoto)


        binding.root.setOnClickListener {
            val context = binding.root.context
            val intent = Intent(context, UpdatePlace::class.java)
            intent.putExtra("place", post.place)
            context.startActivity(intent)
        }

        binding.root.setOnLongClickListener{
            onPostLongclick(post)
            true

        }
    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = YourplaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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