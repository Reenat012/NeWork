package com.example.nework2.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.databinding.FragmentNewPostBinding
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.util.AppConst
import com.example.nework2.util.focusAndShowKeyboard
import com.example.nework2.view.loadAttachment
import com.example.nework2.viewmodel.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private var placeMark: PlacemarkMapObject? = null
    private val gson = Gson()
    private val pointToken = object : TypeToken<Point>() {}.type
    private val usersToken = object : TypeToken<List<Long>>() {}.type

    private val startForPhotoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    val file = fileUri.toFile()

                    postViewModel.setAttachment(fileUri, file, AttachmentType.IMAGE)
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.task_cancelled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val file = uri.toFile(requireContext())!!
                val size = file.length()
                if (size > 15728640) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.attachment_15MB),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@registerForActivityResult
                }
                postViewModel.setAttachment(
                    uri,
                    file,
                    AttachmentType.VIDEO
                )
            }
        }

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        val arg = arguments?.getString(AppConst.EDIT_POST)
        if (arg != null) {
            binding.content.setText(arg)
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    postViewModel.savePost(binding.content.text.toString())
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        binding.addPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent {
                    startForPhotoResult.launch(it)
                }
        }

        binding.addFile.setOnClickListener {
            pickVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }

        binding.clearPhoto.setOnClickListener {
            postViewModel.removePhoto()
        }

        binding.addLocation.setOnClickListener {
            findNavController().navigate(R.id.action_newPostFragment_to_mapsFragment)
        }
        setFragmentResultListener(AppConst.MAPS_FRAGMENT_RESULT) { _, bundle ->
            val point = gson.fromJson<Point>(bundle.getString(AppConst.MAP_POINT), pointToken)
            if (point != null) {
                postViewModel.setCoord(point)
            }
        }

        binding.removeLocation.setOnClickListener {
            postViewModel.removeCoords()
        }

        binding.addUser.setOnClickListener {
            findNavController().navigate(
                R.id.action_newPostFragment_to_usersFragment,
                bundleOf(AppConst.SELECT_USER to true)
            )
        }
        setFragmentResultListener(AppConst.USERS_FRAGMENT_RESULT) { _, bundle ->
            val selectedUsers =
                gson.fromJson<List<Long>>(bundle.getString(AppConst.SELECT_USER), usersToken)
            if (selectedUsers != null) {
                postViewModel.setMentionId(selectedUsers)
            }
        }


        postViewModel.attachmentData.observe(viewLifecycleOwner) { attachment ->
            when (attachment?.attachmentType) {
                AttachmentType.IMAGE -> {
//                   binding.previewPhoto.loadAttachment(attachment.uri.toString())
                    binding.previewPhoto.setImageURI(attachment.uri)
                    binding.previewContainer.isVisible = true
                }
                AttachmentType.VIDEO -> {}

                AttachmentType.AUDIO -> {}
                null -> {
                    binding.previewContainer.isVisible = false
                }
            }
        }

        val imageProvider =
            ImageProvider.fromResource(requireContext(), R.drawable.icon_loacation_without_back)
        postViewModel.editedPost.observe(viewLifecycleOwner) { post ->
            val point = if (post.coords != null) Point(post.coords.lat, post.coords.long) else null
            if (point != null) {
                if (placeMark == null) {
                    placeMark = binding.map.mapWindow.map.mapObjects.addPlacemark()
                }
                placeMark?.apply {
                    geometry = point
                    setIcon(imageProvider)
                    isVisible = true
                }
                binding.map.mapWindow.map.move(
                    CameraPosition(
                        point,
                        13.0f,
                        0f,
                        0f
                    )
                )
            } else {
                placeMark = null
            }
            binding.mapContainer.isVisible = placeMark != null
        }

        binding.itemContainer.setOnClickListener {
            binding.content.focusAndShowKeyboard()
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

}


fun Uri.toFile(context: Context): File? {
    val inputStream = context.contentResolver.openInputStream(this)
    val tempFile = File.createTempFile("temp", ".jpg")

    val outputStream = FileOutputStream(tempFile)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}
