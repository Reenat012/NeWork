package com.example.nework2.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.databinding.FragmentAuth2Binding
import com.example.nework2.util.StringArg
import com.example.nework2.util.TextCallback
import com.example.nework2.viewmodel.AuthViewModel
import com.example.nework2.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AuthFragment2 : Fragment(), TextCallback {

    //передаем текст в репозиторий для обработки
    companion object {
        var Bundle.textArg: String? by StringArg

    }

    private val loginViewModel: LoginViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuth2Binding.inflate(
            inflater,
            container,
            false
        )

        binding.signInButton.setOnClickListener {
            //пробрасываем логин и пароль во authViewModel
            onLoginReceived(binding.login.text.toString())
            onPasswordReceived(binding.password.text.toString())

            loginViewModel.onLoginTap()
        }

        authViewModel.authData.observe(viewLifecycleOwner) { // <---
            if (it!= null) {
                binding.progressBar.visibility = View.GONE
                //переходим обратно в feedFragment
                findNavController().navigateUp()
            }
        }

        loginViewModel.progressRegister.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it != null

        }

        loginViewModel.errorEvent.observe(viewLifecycleOwner) {
            if (it != null) {
                Snackbar.make(binding.root, R.string.network_error, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        return binding.root
    }

    override fun onLoginReceived(text: String) {
        loginViewModel.saveLogin(text)
    }

    override fun onPasswordReceived(text: String) {
        loginViewModel.savePassword(text)
    }

    override fun onRetryPasswordReceived(text: String) {
    }

    override fun onNameReceived(text: String) {
    }


}