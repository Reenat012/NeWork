package com.example.nework2.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.nework2.R
import com.example.nework2.databinding.FragmentFeedBinding
import com.example.nework2.model.AuthModel
import com.example.nework2.util.AppConst
import com.example.nework2.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment(
) : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        var token: AuthModel? = null
        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            token = state
        }


        val childNavHostFragment =
            childFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val childNavController = childNavHostFragment.navController
        binding.bottomNavigation.setupWithNavController(childNavController)

        binding.topAppBar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.user -> {
                    if (token?.id != 0L && token?.id.toString().isNotEmpty()) {
                        findNavController().navigate(
                            R.id.action_feedFragment_to_detailUserFragment,
                            bundleOf(AppConst.USER_ID to token?.id)
                        )
                    } else {
                        findNavController().navigate(R.id.action_feedFragment_to_authFragment2)
                    }
                    true
                }

                else -> false
            }
        }

        return binding.root
    }

}









