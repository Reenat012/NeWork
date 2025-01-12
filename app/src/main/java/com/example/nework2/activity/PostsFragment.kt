package com.example.nework2.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import com.example.nework2.R
import com.example.nework2.adapter.OnInteractionListener
import com.example.nework2.adapter.PostAdapter
import com.example.nework2.adapter.PostViewHolder
import com.example.nework2.databinding.FragmentPostsBinding
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Post
import com.example.nework2.dto.UserResponse
import com.example.nework2.model.AuthModel
import com.example.nework2.util.AppConst
import com.example.nework2.viewmodel.AuthViewModel
import com.example.nework2.viewmodel.PostViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@AndroidEntryPoint
class PostsFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostsBinding.inflate(inflater, container, false)
        val parentNavController = parentFragment?.parentFragment?.findNavController()

        var token: AuthModel? = null
        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            token = state
        }

        val userId = arguments?.getLong(AppConst.USER_ID)

        val postAdapter = PostAdapter(object : OnInteractionListener {
            override fun like(feedItem: FeedItem) {
                if (token?.id != 0L && token?.id.toString().isNotEmpty()) {
                    postViewModel.like(feedItem as Post)
                } else {
                    parentNavController?.navigate(R.id.action_feedFragment_to_authFragment2)
                }
            }

            override fun delete(feedItem: FeedItem) {
                postViewModel.deletePost(feedItem as Post)
            }

            @SuppressLint("NewApi")
            override fun edit(feedItem: FeedItem) {
                feedItem as Post
                postViewModel.edit(feedItem)
                parentNavController?.navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    bundleOf(AppConst.EDIT_POST to feedItem.content)
                )
            }

            override fun selectUser(userResponse: UserResponse) {}

            override fun openCard(feedItem: FeedItem) {
                postViewModel.openPost(feedItem as Post)
                parentNavController?.navigate(R.id.action_feedFragment_to_detailPostFragment)
            }
        })

        binding.recyclerViewPost.adapter = postAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                postViewModel.data.collectLatest {
                    if (userId != null) {
                        postAdapter.submitData(it.filter { feedItem ->
                            feedItem is Post && feedItem.authorId == userId
                        })
                    } else {
                        postAdapter.submitData(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            postAdapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing =
                    it.refresh is LoadState.Loading
                if (it.append is LoadState.Error
                    || it.prepend is LoadState.Error
                    || it.refresh is LoadState.Error
                ) {
                    Snackbar.make(
                        binding.root,
                        R.string.connection_error,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                suspendCancellableCoroutine {
                    it.invokeOnCancellation {
                        (0..<binding.recyclerViewPost.childCount)
                            .map(binding.recyclerViewPost::getChildAt)
                            .map(binding.recyclerViewPost::getChildViewHolder)
                            .filterIsInstance<PostViewHolder>()
                            .onEach(PostViewHolder::stopPlayer)
                    }
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            postAdapter.refresh()
        }

        binding.buttonNewPost.isVisible = userId == null
        binding.buttonNewPost.setOnClickListener {
            if (token?.id != 0L && token?.id.toString().isNotEmpty()) {
                parentNavController?.navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                parentNavController?.navigate(R.id.action_feedFragment_to_authFragment2)
            }
        }

        return binding.root
    }

}