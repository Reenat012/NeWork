package com.example.nework2.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.nework2.activity.JobsFragment
import com.example.nework2.activity.PostsFragment
import com.example.nework2.util.AppConst

class PagerAdapter(fragment: Fragment, private val userId: Long?) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                PostsFragment().apply {
                    arguments = bundleOf(AppConst.USER_ID to userId)
                }
            }

            1 -> {
                JobsFragment.newInstance().apply {
                    arguments = bundleOf(AppConst.USER_ID to userId)
                }
            }

            else -> {
                error("Unknown position")
            }
        }
    }
}