package com.example.nework2.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.databinding.FragmentAuth2Binding
import com.example.nework2.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AuthFragment2 : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    private var login = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuth2Binding.inflate(inflater, container, false)

        binding.textLogin.addTextChangedListener {
            login = it.toString()
            binding.apply {
                login.error = null
                signInButton.isChecked = updateStateButtonLogin()
            }
        }

        binding.textPassword.addTextChangedListener {
            password = it.toString()
            binding.apply {
                password.error = null
                signInButton.isChecked = updateStateButtonLogin()
            }
        }

        binding.signInButton.setOnClickListener {
            login.trim()
            password.trim()
            when {
                password.isEmpty() && login.isEmpty() -> {
                    binding.apply {
                        login.error = getString(R.string.empty_login)
                        password.error = getString(R.string.empty_password)
                    }
                }

                password.isEmpty() -> {
                    binding.password.error = getString(R.string.empty_password)
                }

                login.isEmpty() -> {
                    binding.login.error = getString(R.string.empty_login)
                }

                else -> {
                    authViewModel.login(login, password)
                }
            }
        }

        binding.ifNoReg.setOnClickListener {
            findNavController().navigate(R.id.action_authFragment2_to_regFragment2)
        }

        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            val token = state.token.toString()

            if (state.id != 0L && token.isNotEmpty()) {
                findNavController().navigateUp()
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun updateStateButtonLogin(): Boolean {
        return login.isNotEmpty() && password.isNotEmpty()
    }


}