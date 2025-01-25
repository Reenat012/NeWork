package com.example.nework2.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.databinding.CardJobBinding
import com.example.nework2.databinding.FragmentJobsBinding
import com.example.nework2.util.AppConst
import com.example.nework2.viewmodel.AuthViewModel
import com.example.nework2.viewmodel.JobViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class JobsFragment : Fragment() {
    private val jobViewModel: JobViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    @SuppressLint("NewApi")
    private val emptyOffsetDateTime = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentJobsBinding.inflate(inflater, container, false)

        val userId = arguments?.getLong(AppConst.USER_ID)
        jobViewModel.getJobs(userId)

        binding.buttonNewJob.isVisible = userId == authViewModel.dataAuth.value?.id

        jobViewModel.data.observe(viewLifecycleOwner) { jobs ->
            binding.containerJob.removeAllViews()
            jobs.forEach { job ->
                CardJobBinding.inflate(layoutInflater, binding.containerJob, true).apply {
                    name.text = job.name
                    startFinish.text = buildString {
                        append(job.start.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                        append(" - ")
                        append(
                            if ((job.finish?.year ?: 0) == emptyOffsetDateTime.year) getString(R.string.present_time) else job.finish?.format(
                                DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            )
                        )
                    }
                    position.text = job.position

                    buttonRemoveJob.isVisible = userId == authViewModel.dataAuth.value?.id
                    buttonRemoveJob.setOnClickListener {
                        jobViewModel.deleteJob(job.id)
                    }
                }.root
            }
        }

        binding.buttonNewJob.setOnClickListener {
            findNavController().navigate(R.id.action_detailUserFragment_to_newJobFragment)
        }

        return binding.root
    }


    companion object {
        fun newInstance() = JobsFragment()
    }
}