package com.example.nework2.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.activity.FeedFragment.Companion.textArg
import com.example.nework2.adapter.OnInteractionListener
import com.example.nework2.adapter.PostViewHolder
import com.example.nework2.databinding.FragmentPostCardBinding
import com.example.nework2.dto.Post
import com.example.nework2.util.LongArg
import com.example.nework2.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostCardFragment : Fragment() {

    val postViewModel: PostViewModel by activityViewModels()

    fun navigation() {
        findNavController().navigate(R.id.action_feedFragment_to_authFragment2)
    }

    companion object {
        var Bundle.idArg: Long by LongArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //надуваем разметку
        val binding = FragmentPostCardBinding.inflate(
            inflater,
            container,
            false
        )

        view?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it.findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val postId = arguments?.idArg

        val viewHolder = PostViewHolder(binding, object : OnInteractionListener {

            override fun onEdit(post: Post) {
                postViewModel.edit(post)
                findNavController().navigate(
                    R.id.action_postCardFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    })
            }

            override fun onLike(post: Post) {
                postViewModel.likeById(post.id)
            }

            override fun onRemove(post: Post) {
                postViewModel.removeById(post.id)
                findNavController().navigateUp()
            }

            override fun onRepost(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }

                val shareIntent = Intent.createChooser(intent, "Share Post")
                startActivity(shareIntent)
            }

            override fun playVideoInUri(post: Post) {
                postViewModel.playVideo(post)
                //получаем url
                val url = post.video.toString()
                //создаем интент
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

                startActivity(intent)
            }

            override fun openPost(post: Post) {
            }

            override fun openImage(post: Post) {
                val uriPhoto = Uri.parse("http://10.0.2.2:9999/media/${post.attachment?.url}")

                findNavController().navigate(
                    R.id.action_feedFragment_to_viewPhotoFragment,
                    bundleOf("uriKey" to uriPhoto) //передаем uri изображения с помощью ключа
                )
            }

        })
//            viewHolder.bind(post)
//        }

        //получаем изображение с сервера и присваиваем его photo_iv
//        Glide.with(this)
//            //получаем последнее значение uri
//            .load("http://10.0.2.2:9999/media/${post.attachment?.url}")
//            .into(binding.photoIv)



        binding.videoView.setOnClickListener {
            //получаем ссылку
            val url = Uri.parse(binding.tvVideoPublished.toString())
            //создаем интент
            val intent = Intent(Intent.ACTION_VIEW, url)
        }

        return binding.root
    }
}