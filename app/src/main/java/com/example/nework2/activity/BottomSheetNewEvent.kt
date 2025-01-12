package com.example.nework2.activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.example.nework2.R
import com.example.nework2.databinding.FragmentBottomSheetNewEventBinding
import com.example.nework2.dto.EventType
import com.example.nework2.viewmodel.EventViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class BottomSheetNewEvent : BottomSheetDialogFragment() {
    private val eventViewModel: EventViewModel by activityViewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm", Locale.getDefault())
    private var date = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBottomSheetNewEventBinding.inflate(inflater, container, false)

        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(getString(R.string.select_date))
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setSelection(System.currentTimeMillis())
            .build()

        binding.textField.setEndIconOnClickListener {
            datePicker.show(childFragmentManager, "tag")
        }

        datePicker.addOnPositiveButtonClickListener {
            val instant = Instant.ofEpochMilli(it)
            val dateString = instant.atOffset(ZoneOffset.UTC).format(formatter)
            binding.dateTextField.setText(dateString)
        }

        binding.dateTextField.addTextChangedListener {
            date = it.toString()
        }

        binding.radioButtonOnline.isChecked = true
        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                (R.id.radioButtonOnline) -> {
                    eventViewModel.setEventType(EventType.ONLINE)
                }

                else -> {
                    eventViewModel.setEventType(EventType.OFFLINE)
                }
            }
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        if (date.isNotEmpty()) {
            try {
                val odt = LocalDateTime.parse(date, formatter).atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
                eventViewModel.setDateTime(odt)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_date_format),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onDestroy()
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

}