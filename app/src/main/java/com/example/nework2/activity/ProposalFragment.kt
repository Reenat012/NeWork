package com.example.nework2.activity

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProposalFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Заблокировано")
                .setIcon(R.drawable.vektor_ng)
                .setCancelable(true)
                .setPositiveButton("Войти") { _, _ ->
                    findNavController().navigate(R.id.action_proposalFragment_to_authFragment2)
                }
                .setNegativeButton(
                    "Отмена"
                ) { _, _ ->
                    findNavController().navigateUp()
                    //выходим из диалогового окна
                    return@setNegativeButton
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}