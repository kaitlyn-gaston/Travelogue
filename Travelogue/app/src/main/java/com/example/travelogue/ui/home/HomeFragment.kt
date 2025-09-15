package com.example.travelogue.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.travelogue.databinding.FragmentHomeBinding
import com.example.travelogue.data.Travelogue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Map of markers to document IDs (Firestore travelogue IDs)
    private val markerIdMap = mutableMapOf<Marker, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager
            .findFragmentById(com.example.travelogue.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener { marker ->
            val travelogueId = markerIdMap[marker]
            val snippetParts = marker.snippet?.split("|") ?: listOf("", "", "", "")

            val dialog = MarkerInfoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("travelogueId", travelogueId) // Pass Firestore doc ID
                    putString("title", marker.title)
                    putString("postedBy", snippetParts.getOrNull(0))
                    putString("place", snippetParts.getOrNull(1))
                    putString("description", snippetParts.getOrNull(2))
                    putString("imageUrl", snippetParts.getOrNull(3))
                }
            }

            dialog.show(parentFragmentManager, "MarkerInfoDialog")
            true
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
            } else {
                Toast.makeText(requireContext(), "Current location not available", Toast.LENGTH_SHORT).show()
            }
        }

        loadTravelogueMarkers()
    }

    private fun loadTravelogueMarkers() {
        val db = FirebaseFirestore.getInstance()

        mMap.clear()
        markerIdMap.clear()

        db.collection("travelogue")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "No travelogues found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (document in result) {
                    val travelogue = document.toObject(Travelogue::class.java)
                    val documentId = document.id

                    val position = LatLng(travelogue.latitude, travelogue.longitude)

                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(travelogue.title)
                            .snippet("${travelogue.userName}|${travelogue.location}|${travelogue.description}|${travelogue.imageUrl}")
                    )

                    marker?.let {
                        markerIdMap[it] = documentId // Store Firestore ID
                    }
                }

                // Focus camera on the first travelogue
                val first = result.documents.firstOrNull()?.toObject(Travelogue::class.java)
                first?.let {
                    val firstLatLng = LatLng(it.latitude, it.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 10f))
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading travelogues", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        if (this::mMap.isInitialized) {
            loadTravelogueMarkers()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}