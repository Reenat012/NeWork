package com.example.nework2.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.adapter.AvatarAdapter
import com.example.nework2.adapter.AvatarItemDecorations
import com.example.nework2.adapter.InvolvedOnClickListener
import com.example.nework2.databinding.FragmentDetailEventBinding
import com.example.nework2.dto.Event
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.model.InvolvedItemType
import com.example.nework2.util.AppConst
import com.example.nework2.view.loadAttachment
import com.example.nework2.view.loadAvatar
import com.example.nework2.viewmodel.EventViewModel
import com.google.gson.Gson
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class DetailEventFragment : Fragment() {
    private val eventViewModel: EventViewModel by activityViewModels()
    private var player: ExoPlayer? = null
    private var placeMark: PlacemarkMapObject? = null
    private val gson = Gson()

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailEventBinding.inflate(inflater, container, false)

        var event: Event? = null
        val avatarDecoration = AvatarItemDecorations(64)

        val speakersAdapter = AvatarAdapter(object : InvolvedOnClickListener {
            override fun openList() {
                findNavController().navigate(
                    R.id.usersFragment,
                    bundleOf(AppConst.SPEAKERS to gson.toJson(event?.speakerIds))
                )
            }
        })
        val likersAdapter = AvatarAdapter(object : InvolvedOnClickListener {
            override fun openList() {
                findNavController().navigate(
                    R.id.usersFragment,
                    bundleOf(AppConst.LIKERS to gson.toJson(event?.likeOwnerIds))
                )
            }
        })
        val participantAdapter = AvatarAdapter(object : InvolvedOnClickListener {
            override fun openList() {
                findNavController().navigate(
                    R.id.usersFragment,
                    bundleOf(AppConst.PARTICIPANT to gson.toJson(event?.participantsIds))
                )
            }
        })

        binding.recyclerSpeaker.apply {
            addItemDecoration(avatarDecoration)
            adapter = speakersAdapter
        }
        binding.recyclerLikers.apply {
            addItemDecoration(avatarDecoration)
            adapter = likersAdapter
        }
        binding.recyclerParticipant.apply {
            addItemDecoration(avatarDecoration)
            adapter = participantAdapter
        }

        val imageProvider =
            ImageProvider.fromResource(requireContext(), R.drawable.baseline_add_location_24)

        eventViewModel.eventData.observe(viewLifecycleOwner) { eventItem ->
            event = eventItem
            with(binding) {
                avatar.loadAvatar(eventItem.authorAvatar)
                authorName.text = eventItem.author
                lastWork.text = eventItem.authorJob ?: getString(R.string.in_search_work)

                when (eventItem.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        imageContent.loadAttachment(eventItem.attachment.url)
                        imageContent.isVisible = true
                    }

                    AttachmentType.VIDEO -> {
                        player = ExoPlayer.Builder(requireContext()).build().apply {
                            setMediaItem(MediaItem.fromUri(eventItem.attachment.url))
                        }
                        videoContent.player = player
                        videoContent.isVisible = true
                    }

                    AttachmentType.AUDIO -> {
                        player = ExoPlayer.Builder(requireContext()).build().apply {
                            setMediaItem(MediaItem.fromUri(eventItem.attachment.url))
                        }
                        videoContent.player = player
                        audioContent.isVisible = true
                    }

                    null -> {
                        imageContent.isVisible = false
                        videoContent.isVisible = false
                        audioContent.isVisible = false
                        player?.release()
                    }
                }


                typeEvent.text = eventItem.type.toString()
                dateEvent.text =
                    eventItem.datetime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                        .toString()
                content.text = eventItem.content

                lifecycleScope.launch {
                    eventViewModel.getInvolved(eventItem.speakerIds, InvolvedItemType.SPEAKERS)
                }

                lifecycleScope.launch {
                    eventViewModel.getInvolved(eventItem.likeOwnerIds, InvolvedItemType.LIKERS)
                }

                lifecycleScope.launch {
                    eventViewModel.getInvolved(
                        eventItem.participantsIds,
                        InvolvedItemType.PARTICIPANT
                    )
                }

                buttonLike.isChecked = eventItem.likedByMe
                buttonLike.text = eventItem.likeOwnerIds.size.toString()

                participantsButton.text = eventItem.participantsIds.size.toString()

                val point =
                    if (eventItem.coords != null) Point(
                        eventItem.coords.lat,
                        eventItem.coords.long
                    ) else null
                if (point != null) {
                    if (placeMark == null) {
                        placeMark = binding.map.mapWindow.map.mapObjects.addPlacemark()
                    }
                    placeMark?.apply {
                        geometry = point
                        setIcon(imageProvider)
                        isVisible = true
                    }
                    binding.map.mapWindow.map.move(
                        CameraPosition(
                            point,
                            13.0f,
                            0f,
                            0f
                        )
                    )
                } else {
                    placeMark = null
                }
                binding.map.isVisible = placeMark != null && point != null

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

            }
        }

        eventViewModel.involvedData.observe(viewLifecycleOwner) { involved ->
            speakersAdapter.submitList(involved.speakers)
            likersAdapter.submitList(involved.likers)
            participantAdapter.submitList(involved.participants)
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        player?.apply {
            stop()
        }
        eventViewModel.resetInvolved()
    }

}