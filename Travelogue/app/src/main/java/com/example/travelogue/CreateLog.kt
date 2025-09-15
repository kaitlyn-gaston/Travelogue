package com.example.travelogue

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

class CreateLog : AppCompatActivity() {
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var imageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_log)

        var gallery_btn = findViewById<ImageButton>(R.id.gallery_button)
        var camera_btn = findViewById<ImageButton>(R.id.camera_button)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val intent = Intent(this, LogWriting::class.java)
                intent.putExtra("image_uri", it.toString())
                startActivity(intent)
            }
        }
        gallery_btn.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        val takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri.let {
                    val intent = Intent(this, LogWriting::class.java)
                    intent.putExtra("image_uri", it.toString())
                    startActivity(intent)
                }
            }
        }
        camera_btn.setOnClickListener{
            val timeStamp = System.currentTimeMillis()
            val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_$timeStamp.jpg")
            imageUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imageFile)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            takePictureLauncher.launch(cameraIntent)
        }
    }
}