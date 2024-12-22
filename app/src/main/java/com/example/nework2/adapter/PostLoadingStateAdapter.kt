package com.example.nework2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nework2.databinding.ItemLoadingBinding

class PostLoadingStateAdapter(
    private val retryListener: () -> Unit
) : LoadStateAdapter<PostLoadingViewHolder>() {
    override fun onBindViewHolder(holder: PostLoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PostLoadingViewHolder = PostLoadingViewHolder(
        ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        retryListener
    )
}

class PostLoadingViewHolder(
    private val itemLoadingBinding: ItemLoadingBinding,
    private val retryListener: () -> Unit
) : RecyclerView.ViewHolder(itemLoadingBinding.root) {
    fun bind(loadState: LoadState) {
        itemLoadingBinding.apply {
            progressBar2.isVisible = loadState is LoadState.Loading
            buttonRetry2.isVisible = loadState is LoadState.Error
            buttonRetry2.setOnClickListener {
                retryListener
            }
        }

    }
}