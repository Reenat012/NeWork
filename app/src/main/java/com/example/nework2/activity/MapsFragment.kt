package com.example.nework2.activity

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.nework2.R
import com.example.nework2.databinding.FragmentMapsBinding
import com.example.nework2.util.AppConst
import com.google.gson.Gson
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider

class MapsFragment : Fragment(), UserLocationObjectListener {
    private lateinit var userLocation: UserLocationLayer
    private var placeMark: PlacemarkMapObject? = null
    private lateinit var map: MapView
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMapsBinding.inflate(inflater, container, false)
        map = binding.map
        MapKitFactory.initialize(requireContext())

        userLocation =
            MapKitFactory.getInstance().createUserLocationLayer(binding.map.mapWindow)
        userLocation.isVisible = true
        userLocation.setObjectListener(this)


        val imageProvider =
            ImageProvider.fromResource(requireContext(), R.drawable.baseline_add_location_24)
        val inputListener = object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) = Unit
            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                if (placeMark == null) {
                    placeMark = binding.map.mapWindow.map.mapObjects.addPlacemark()
                }
                placeMark?.apply {
                    geometry = point
                    setIcon(imageProvider)
                    isVisible = true
                }
            }
        }
        binding.map.mapWindow.map.addInputListener(inputListener)

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    setFragmentResult(
                        AppConst.MAPS_FRAGMENT_RESULT,
                        bundleOf(AppConst.MAP_POINT to gson.toJson(placeMark?.geometry))
                    )
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        map.onStart()
    }

    override fun onStop() {
        map.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onObjectAdded(userLoactionView: UserLocationView) {
        userLocation.setAnchor(
            PointF((map.width * 0.5).toFloat(), (map.height * 0.5).toFloat()),
            PointF((map.width * 0.5).toFloat(), (map.height * 0.83).toFloat())
        )
        map.mapWindow.map.move(
            CameraPosition(userLoactionView.arrow.geometry, 17f, 0f, 0f)
        )
    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }

}