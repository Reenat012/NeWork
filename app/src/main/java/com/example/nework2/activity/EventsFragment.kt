package com.example.nework2.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.nework2.R
import com.example.nework2.adapter.EventAdapter
import com.example.nework2.adapter.EventViewHolder
import com.example.nework2.adapter.OnInteractionListener
import com.example.nework2.databinding.FragmentEventsBinding
import com.example.nework2.dto.Event
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.UserResponse
import com.example.nework2.model.AuthModel
import com.example.nework2.util.AppConst
import com.example.nework2.viewmodel.AuthViewModel
import com.example.nework2.viewmodel.EventViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@AndroidEntryPoint
class EventsFragment : Fragment() {
    private val eventViewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventsBinding.inflate(inflater, container, false)
        val parentNavController = parentFragment?.parentFragment?.findNavController()

        var token: AuthModel? = null
        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            token = state
        }

        val eventAdapter = EventAdapter(object : OnInteractionListener {
            override fun like(feedItem: FeedItem) {
                if (token?.id != 0L && token?.id.toString().isNotEmpty()) {
                    eventViewModel.like(feedItem as Event)
                } else {
                    parentNavController?.navigate(R.id.action_feedFragment_to_newEventFragment)
                }
            }

            override fun delete(feedItem: FeedItem) {
                eventViewModel.deleteEvent(feedItem as Event)
            }

            @SuppressLint("NewApi")
            override fun edit(feedItem: FeedItem) {
                feedItem as Event
                eventViewModel.edit(feedItem)
                parentNavController?.navigate(
                    R.id.action_feedFragment_to_newEventFragment,
                    bundleOf(AppConst.EDIT_EVENT to feedItem.content)
                )
            }

            override fun selectUser(userResponse: UserResponse) {}

            override fun openCard(feedItem: FeedItem) {
                eventViewModel.openEvent(feedItem as Event)
                parentNavController?.navigate(R.id.action_feedFragment_to_detailEventFragment)
            }
        })

        binding.recyclerViewEvent.adapter = eventAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                eventViewModel.data.collectLatest {
                    eventAdapter.submitData(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            eventAdapter.loadStateFlow.collectLatest {
                binding.swipeRefreshEvent.isRefreshing =
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
                        (0..<binding.recyclerViewEvent.childCount)
                            .map(binding.recyclerViewEvent::getChildAt)
                            .map(binding.recyclerViewEvent::getChildViewHolder)
                            .filterIsInstance<EventViewHolder>()
                            .onEach(EventViewHolder::stopPlayer)
                    }
                }
            }
        }

        binding.swipeRefreshEvent.setOnRefreshListener {
            eventAdapter.refresh()
        }

        binding.buttonNewEvent.setOnClickListener {
            if (token?.id != 0L && token?.id.toString().isNotEmpty()) {
                parentNavController?.navigate(R.id.action_feedFragment_to_newEventFragment)
            } else {
                parentNavController?.navigate(R.id.action_feedFragment_to_authFragment2)
            }
        }


        return binding.root
    }

}