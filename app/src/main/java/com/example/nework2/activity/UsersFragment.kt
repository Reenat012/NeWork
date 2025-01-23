package com.example.nework2.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import com.example.nework2.R
import com.example.nework2.adapter.OnInteractionListener
import com.example.nework2.adapter.UserAdapter
import com.example.nework2.databinding.FragmentUsersBinding
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.UserResponse
import com.example.nework2.util.AppConst
import com.example.nework2.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsersFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Long>>() {}.type

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersBinding.inflate(inflater, container, false)
        val parentNavController = parentFragment?.parentFragment?.findNavController()

        val arg = arguments?.getBoolean(AppConst.SELECT_USER) ?: false
        val selectedUsers = mutableListOf<Long>()

        val involved = when {
            arguments?.containsKey(AppConst.SPEAKERS) == true -> {
                binding.topAppBar.title = getString(R.string.speakers)
                gson.fromJson<List<Long>>(arguments?.getString(AppConst.SPEAKERS), typeToken)
            }

            arguments?.containsKey(AppConst.LIKERS) == true -> {
                binding.topAppBar.title = getString(R.string.likers)
                gson.fromJson<List<Long>>(arguments?.getString(AppConst.LIKERS), typeToken)
            }

            arguments?.containsKey(AppConst.PARTICIPANT) == true -> {
                binding.topAppBar.title = getString(R.string.participants)
                gson.fromJson<List<Long>>(arguments?.getString(AppConst.PARTICIPANT), typeToken)
            }

            arguments?.containsKey(AppConst.MENTIONED) == true -> {
                binding.topAppBar.title = getString(R.string.mentioned)
                gson.fromJson<List<Long>>(arguments?.getString(AppConst.MENTIONED), typeToken)
            }

            else -> null
        }


        val userAdapter = UserAdapter(object : OnInteractionListener {
            override fun like(feedItem: FeedItem) {}
            override fun delete(feedItem: FeedItem) {}
            override fun edit(feedItem: FeedItem) {}

            override fun selectUser(userResponse: UserResponse) {
                if (selectedUsers.contains(userResponse.id)) {
                    selectedUsers.remove(userResponse.id)
                } else {
                    selectedUsers.add(userResponse.id)
                }
            }

            override fun openCard(feedItem: FeedItem) {
                parentNavController?.navigate(
                    R.id.action_feedFragment_to_detailUserFragment,
                    bundleOf(AppConst.USER_ID to feedItem.id)
                )
            }

        }, arg, selectedUsers)
        binding.recyclerViewUser.adapter = userAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.dataUsers.collectLatest {
                    if (involved != null) {
                        userAdapter.submitData(
                            it.filter { item ->
                                item.id in involved
                            })
                    } else {
                        userAdapter.submitData(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            userAdapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing =
                    it.refresh is LoadState.Loading

                if (it.refresh is LoadState.Error) {
                    Snackbar.make(
                        binding.root,
                        R.string.connection_error,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            userAdapter.refresh()
        }

        binding.topAppBar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                when {
                    arg -> {
                        menuInflater.inflate(R.menu.menu_add_post, menu)
                        binding.topAppBar.title = AppConst.SELECT_USER
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        setFragmentResult(
                            AppConst.USERS_FRAGMENT_RESULT,
                            bundleOf(AppConst.SELECT_USER to gson.toJson(selectedUsers))
                        )
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }
            }
        })

        binding.topAppBar.isVisible = arg || involved != null

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}