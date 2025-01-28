package com.example.nework2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nework2.R
import com.example.nework2.databinding.CardAvatarBinding
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.UserResponse
import com.example.nework2.view.load
import com.example.nework2.view.loadAvatar


class AvatarAdapter(
    private val involvedOnClickListener: InvolvedOnClickListener
) : ListAdapter<FeedItem, AvatarViewHolder>(FeedItemCallBack()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val binding = CardAvatarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AvatarViewHolder(binding, involvedOnClickListener)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        if (position == 5) {
            holder.addButton()
        } else {
            val user = getItem(position) as UserResponse
            holder.bind(user)
        }
    }

    override fun getItemCount(): Int {
        val size = currentList.size
        return if (size > 4) size + 1 else size
    }

}

class AvatarViewHolder(
    private val binding: CardAvatarBinding,
    private val involvedOnClickListener: InvolvedOnClickListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: UserResponse) {
        binding.avatar.loadAvatar(user.avatar)
    }

    fun addButton() {
        binding.avatar.setImageResource(R.drawable.add_icon)
        binding.avatar.isClickable = true
        binding.avatar.setOnClickListener {
            involvedOnClickListener.openList()
        }
    }

}