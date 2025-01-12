package com.example.nework2.activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import com.example.nework2.databinding.FragmentDetailPostBinding
import com.example.nework2.dto.Post
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.model.InvolvedItemType
import com.example.nework2.util.AppConst
import com.example.nework2.view.loadAttachment
import com.example.nework2.view.loadAvatar
import com.example.nework2.viewmodel.PostViewModel
import com.google.gson.Gson
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DetailPostFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private var player: ExoPlayer? = null
    private var placeMark: PlacemarkMapObject? = null
    private val gson = Gson()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailPostBinding.inflate(inflater, container, false)

        var post: Post? = null

        val avatarDecoration = AvatarItemDecorations(64)

        val likersAdapter = AvatarAdapter(object : InvolvedOnClickListener {
            override fun openList() {
                findNavController().navigate(
                    R.id.usersFragment,
                    bundleOf(AppConst.LIKERS to gson.toJson(post?.likeOwnerIds))
                )
            }
        })
        val mentionedAdapter = AvatarAdapter(object : InvolvedOnClickListener {
            override fun openList() {
                findNavController().navigate(
                    R.id.usersFragment,
                    bundleOf(AppConst.MENTIONED to gson.toJson(post?.mentionIds))
                )
            }
        })

        binding.recyclerLikers.apply {
            addItemDecoration(avatarDecoration)
            adapter = likersAdapter
        }
        binding.recyclerMentioned.apply {
            addItemDecoration(avatarDecoration)
            adapter = mentionedAdapter
        }

        val imageProvider =
            ImageProvider.fromResource(requireContext(), R.drawable.baseline_add_location_24)

        postViewModel.postData.observe(viewLifecycleOwner) { postItem ->
            post = postItem
            with(binding) {
                ivAvatar.loadAvatar(postItem.authorAvatar)
                tvAuthor.text = postItem.author
                datePubl.text = postItem.authorJob ?: getString(R.string.in_search_work)

                when (postItem.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        imageContent.loadAttachment(postItem.attachment.url)
                        imageContent.isVisible = true
                    }

                    AttachmentType.VIDEO -> {
                        player = ExoPlayer.Builder(requireContext()).build().apply {
                            setMediaItem(MediaItem.fromUri(postItem.attachment.url))
                        }
                        videoContent.player = player
                        videoContent.isVisible = true
                    }

                    AttachmentType.AUDIO -> {
                        player = ExoPlayer.Builder(requireContext()).build().apply {
                            setMediaItem(MediaItem.fromUri(postItem.attachment.url))
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

                datePubl.text =
                    postItem.published.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                tvContent.text = postItem.content

                lifecycleScope.launch {
                    postViewModel.getInvolved(postItem.likeOwnerIds, InvolvedItemType.LIKERS)
                }

                lifecycleScope.launch {
                    postViewModel.getInvolved(postItem.mentionIds, InvolvedItemType.MENTIONED)
                }


                buttonLike.text = postItem.likeOwnerIds.size.toString()
                buttonLike.isChecked = postItem.likedByMe
                buttonMentioned.text = postItem.mentionIds.size.toString()

                val point =
                    if (postItem.coords != null) Point(
                        postItem.coords.lat,
                        postItem.coords.long
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
                            if (isPlaying) R.drawable.baseline_block_24 else R.drawable.icon_play
                        )
                    }
                })

            }
        }

        postViewModel.involvedData.observe(viewLifecycleOwner) { involved ->
            likersAdapter.submitList(involved.likers)
            mentionedAdapter.submitList(involved.mentioned)
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
        postViewModel.resetInvolved()
    }

}