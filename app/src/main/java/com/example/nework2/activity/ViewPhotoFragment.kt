package com.example.nework2.activity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nework2.databinding.FragmentViewPhotoBinding
import com.example.nework2.view.loadWithoutCircle
import com.example.nework2.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewPhotoFragment : Fragment() {

    val viewModel: PostViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //надуваем разметку
        val binding = FragmentViewPhotoBinding.inflate(
            inflater,
            container,
            false
        )

        // Получаем данные
        val uri: Uri? by lazy {
            requireArguments().getParcelable<Uri>("uriKey")
        }

        //показываем фото
        binding.photoViewIv.loadWithoutCircle(uri.toString())

        return binding.root
    }
}