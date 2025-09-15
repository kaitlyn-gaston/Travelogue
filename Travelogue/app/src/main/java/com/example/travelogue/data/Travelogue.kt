package com.example.travelogue.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.storage.StorageReference

@Parcelize
data class Travelogue(
    var id: String = "",
    val title: String = "",
    val location: String = "",
    val date: Long = 0L,
    val description: String = "",
    val userId: String = "",
    val userName: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable {
    // Empty constructor required for Firebase
    constructor() : this("", "", "", 0L, "", "", "","", 0.0, 0.0)
} 