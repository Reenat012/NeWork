package com.example.nework2.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.databinding.FragmentReg2Binding

import com.example.nework2.util.TextCallback
import com.example.nework2.viewmodel.AuthViewModel
import com.example.nework2.viewmodel.RegistrationViewModel
import com.github.dhaval2404.imagepicker.ImagePicker

import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegFragment : Fragment(), TextCallback {

    companion object {
        fun newInstance() = RegFragment()
    }

    private val viewModel: RegistrationViewModel by viewModels()
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
        val binding = FragmentReg2Binding.inflate(
            inflater,
            container,
            false
        )

        //нажатие кнопки sign up
        binding.signInButton.setOnClickListener {
            //пробрасываем логин, пароль во registrationViewModel
            onNameReceived(binding.etRegName.text.toString())
            onLoginReceived(binding.etRegLogin.text.toString())
            onPasswordReceived(binding.etRegPassword.text.toString())
            onRetryPasswordReceived(binding.etRegRetryPassword.text.toString())

            //проверяем заполнены ли все поля регистрации
            if (viewModel.checkFieldRegister() && viewModel.checkPassword()) {
                viewModel.onSignUpTap()
            } else if (!viewModel.checkFieldRegister() && viewModel.checkPassword()) {
                Toast.makeText(requireContext(), R.string.empty, Toast.LENGTH_SHORT)
                    .show()
            } else if (viewModel.checkFieldRegister() && !viewModel.checkPassword()) {
                Toast.makeText(requireContext(), R.string.password_different, Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.field_empty_password_different,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        //подписываем на хранилище токинов, как только там появится новый токен переходим в feedfragment
        authViewModel.authData.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.progressBar.visibility = View.GONE
                findNavController().navigate(
                    R.id.action_regFragment2_to_feedFragment
                )
            }
        }

        viewModel.progressRegister.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) {
            if (it != null) {
                Snackbar.make(binding.root, R.string.network_error, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        //создаем лаунчер для интента фото
        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            //если ошибка
            if (it.resultCode == ImagePicker.RESULT_ERROR) {
                //показываем всплывающеее окно
                Toast.makeText(requireContext(), R.string.image_pick_error, Toast.LENGTH_SHORT)
                    .show()
                //возврата управления из лямбда-выражения, аннотированного ключевым словом suspend, обратно в точку вызова
                //это позволяет избежать вложенных обратных вызовов и делает код более читаемым и поддерживаемым
                return@registerForActivityResult
            }

            val uri = it.data?.data ?: return@registerForActivityResult

            viewModel.setPhoto(uri, uri.toFile())
        }

        //оформляем подписку на photo
        viewModel.photo.observe(viewLifecycleOwner)
        { model ->
            //если модель есть
            if (model != null) {
                //то заполняем preview тем, что там указано
                binding.ivPrevAvatar.setImageURI(model.uri)
                //показывем preview
                binding.ivPrevAvatar.visibility = View.VISIBLE
            } else {
                //скрываем видимость всего контейнера preview
                binding.ivPrevAvatar.visibility = View.GONE
            }
        }


        //обработчик нажатия на кнопку взять фото из галереи
        binding.buttonDowAvatar.setOnClickListener {
            ImagePicker.Builder(this)
                .crop() //обрезка изображения после его выбора из галерии или камеры
                .galleryOnly()
                .maxResultSize(NewPostFragment.IMAGE_MAX_SIZE, NewPostFragment.IMAGE_MAX_SIZE)
                .createIntent {
                    launcher.launch(it)
                }
        }


        return binding.root
    }

    override fun onLoginReceived(text: String) {
        viewModel.saveLogin(text)
    }

    override fun onPasswordReceived(text: String) {
        viewModel.savePassword(text)
    }

    override fun onRetryPasswordReceived(text: String) {
        viewModel.saveRetryPassword(text)
    }

    override fun onNameReceived(text: String) {
        viewModel.saveName(text)
    }
}