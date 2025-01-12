package com.example.nework2.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nework2.R
import com.example.nework2.databinding.FragmentPostCardBinding
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Post
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.view.loadAttachment
import com.example.nework2.view.loadAvatar
import java.time.format.DateTimeFormatter

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, PostViewHolder>(FeedItemCallBack()) {

    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            FragmentPostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener, parent.context)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position) as Post
        holder.bind(item)
    }
}

class PostViewHolder(
    private val binding: FragmentPostCardBinding,
    private val onInteractionListener: OnInteractionListener,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    private var player: ExoPlayer? = null

    @SuppressLint("NewApi", "SetTextI18n")
    fun bind(post: Post) {
        with(binding) {
            ivAvatar.loadAvatar(post.authorAvatar)
            tvAuthor.text = post.author
            datePubl.text =
                post.published.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            tvContent.text = post.content
            ivLikes.text = post.likeOwnerIds.size.toString()
            ivLikes.isChecked = post.likedByMe

            fun setAttachmentVisibility(
                imageContentVisible: Boolean = false,
                videoContentVisible: Boolean = false,
                audioContentVisible: Boolean = false,
            ) {
                photoIv.isVisible = imageContentVisible
                videoContent.isVisible = videoContentVisible
                audioContent.isVisible = audioContentVisible
            }

            when (post.attachment?.type) {
                AttachmentType.IMAGE -> {
                    photoIv.loadAttachment(post.attachment.url)
                    setAttachmentVisibility(imageContentVisible = true)
                }

                AttachmentType.VIDEO -> {
                    player = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(post.attachment.url))
                    }
                    videoContent.player = player
                    setAttachmentVisibility(videoContentVisible = true)
                }

                AttachmentType.AUDIO -> {
                    player = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(post.attachment.url))
                    }
                    setAttachmentVisibility(audioContentVisible = true)
                }

                null -> {
                    releasePlayer()
                    setAttachmentVisibility()
                }
            }

            playPauseAudio.setOnClickListener {
                if (player?.isPlaying == true) {
                    player!!.playWhenReady = !player!!.playWhenReady
                } else {
                    player?.apply {
                        prepare()
                        play()
                    }
                }
            }

            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    binding.playPauseAudio.setIconResource(
                        if (isPlaying) R.drawable.icon_pause else R.drawable.icon_play
                    )
                }
            })

            ivLikes.setOnClickListener {
                onInteractionListener.like(post)
            }

            ivMenu.isVisible = post.ownedByMe
            ivMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_remove -> {
                                onInteractionListener.delete(post)
                                true
                            }

                            R.id.menu_edit -> {
                                onInteractionListener.edit(post)
                                true
                            }

                            else -> false
                        }
                    }
                    gravity = Gravity.END
                }
                    .show()
            }

            binding.cardPost.setOnClickListener {
                onInteractionListener.openCard(post)
            }


        }
    }

    fun releasePlayer() {
        player?.apply {
            stop()
            release()
        }
    }

    fun stopPlayer() {
        player?.stop()
    }

}