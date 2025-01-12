package com.example.nework2.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.databinding.FragmentNewJobBinding
import com.example.nework2.viewmodel.JobViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class NewJobFragment : Fragment() {
    private val jobViewModel: JobViewModel by activityViewModels()
    @SuppressLint("NewApi")
    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault())
    @SuppressLint("NewApi")
    private val emptyOffsetDateTime =
        OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

    private var flagEmptyField = false
    var name = ""
    var position = ""
    var link = ""

    @SuppressLint("NewApi", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(inflater, container, false)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.nameTextField.addTextChangedListener {
            name = it.toString()
            flagEmptyField = false
            binding.apply {
                nameLayout.error = null
                buttonJobCreate.isChecked = updateStateButtonLogin()
            }
        }
        binding.positionTextField.addTextChangedListener {
            position = it.toString()
            flagEmptyField = false
            binding.apply {
                positionLayout.error = null
                buttonJobCreate.isChecked = updateStateButtonLogin()
            }
        }
        binding.linkTextField.addTextChangedListener {
            link = it.toString()
        }

        binding.buttonJobCreate.setOnClickListener {
            name.trim()
            position.trim()
            link.trim()

            if (name.isEmpty()) {
                binding.nameLayout.error = getString(R.string.empty_field)
                flagEmptyField = true
            }
            if (position.isEmpty()) {
                binding.positionLayout.error = getString(R.string.empty_field)
                flagEmptyField = true
            }

            if (flagEmptyField) {
                return@setOnClickListener
            }

            val startWork = binding.startWork.text.toString()
            val finishWork = binding.finishWork.text.toString()

            try {
                val dateStart = LocalDate.parse(startWork, formatter)
                    .atTime(0, 0)
                    .atOffset(ZoneOffset.UTC)
                val dateFinish =
                    if (finishWork == getString(R.string.present_time)) null else LocalDate.parse(
                        finishWork,
                        formatter
                    )
                        .atTime(0, 0)
                        .atOffset(ZoneOffset.UTC)

                jobViewModel.saveJob(
                    name,
                    position,
                    link,
                    dateStart,
                    dateFinish ?: emptyOffsetDateTime
                )
                findNavController().navigateUp()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_date_format),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
        }

        binding.datePicker.setOnClickListener {
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_date_picker, null)
            val dateStart = dialogView.findViewById<TextInputEditText>(R.id.dateStart)
            val dateFinish = dialogView.findViewById<TextInputEditText>(R.id.dateFinish)
            MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    binding.buttonJobCreate.isChecked = updateStateButtonLogin()
                    binding.startWork.text = dateStart.text.toString().trim()
                    binding.finishWork.text =
                        if (dateFinish.text!!.isEmpty())
                            getString(R.string.present_time)
                        else dateFinish.text.toString().trim()
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                .show()
        }

        return binding.root
    }

    private fun updateStateButtonLogin(): Boolean {
        return name.isNotEmpty() && position.isNotEmpty()
    }

}