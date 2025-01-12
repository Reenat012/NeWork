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
import com.example.nework2.databinding.CardEventBinding
import com.example.nework2.dto.Event
import com.example.nework2.dto.EventType
import com.example.nework2.dto.FeedItem
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.view.loadAttachment
import com.example.nework2.view.loadAvatar
import java.time.format.DateTimeFormatter

class EventAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, EventViewHolder>(FeedItemCallBack()) {

    override fun onViewRecycled(holder: EventViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding =
            CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onInteractionListener, parent.context)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = getItem(position) as Event
        holder.bind(item)
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionListener: OnInteractionListener,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    private var player: ExoPlayer? = null

    @SuppressLint("NewApi", "SetTextI18n")
    fun bind(event: Event) {
        with(binding) {
            avatar.loadAvatar(event.authorAvatar)
            authorName.text = event.author
            datePublication.text =
                event.published.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            content.text = event.content
            typeEvent.text = event.type.toString()
            dateEvent.text = event.datetime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            buttonLike.text = event.likeOwnerIds.size.toString()
            buttonLike.isChecked = event.likedByMe

            buttonPlayEvent.isVisible = event.type == EventType.ONLINE
            buttonOption.isVisible = event.ownedByMe

            fun setAttachmentVisibility(
                imageContentVisible: Boolean = false,
                videoContentVisible: Boolean = false,
                audioContentVisible: Boolean = false,
            ) {
                imageContent.isVisible = imageContentVisible
                videoContent.isVisible = videoContentVisible
                audioContent.isVisible = audioContentVisible
            }

            when (event.attachment?.type) {
                AttachmentType.IMAGE -> {
                    imageContent.loadAttachment(event.attachment.url)
                    setAttachmentVisibility(imageContentVisible = true)
                }

                AttachmentType.VIDEO -> {
                    player = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(event.attachment.url))
                    }
                    videoContent.player = player
                    setAttachmentVisibility(videoContentVisible = true)
                }

                AttachmentType.AUDIO -> {
                    player = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(event.attachment.url))
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

            buttonGroup.text = event.speakerIds.size.toString()

            buttonOption.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_remove -> {
                                onInteractionListener.delete(event)
                                true
                            }

                            R.id.menu_edit -> {
                                onInteractionListener.edit(event)
                                true
                            }

                            else -> false
                        }
                    }
                    gravity = Gravity.END
                }
                    .show()
            }

            buttonLike.setOnClickListener {
                onInteractionListener.like(event)
            }

            cardEvent.setOnClickListener {
                onInteractionListener.openCard(event)
            }


        }
    }

    fun releasePlayer() {
        player?.apply {
            stop()
            release()
        }
    }

    fun stopPlayer(){
        player?.stop()
    }

}