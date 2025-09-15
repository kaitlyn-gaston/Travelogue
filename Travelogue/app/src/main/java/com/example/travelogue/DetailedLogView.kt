package com.example.travelogue

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.travelogue.data.Travelogue

class DetailedLogView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_log_view)
        val travelogue = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("travelogue", Travelogue::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Travelogue>("travelogue")
        }
        if (travelogue != null) {
            findViewById<TextView>(R.id.log_title).text = travelogue.title
            findViewById<TextView>(R.id.caption).text = travelogue.description
            findViewById<TextView>(R.id.poster).text = String.format("Posted by: %s @ %s", travelogue.userName, travelogue.location)
            val imageView = findViewById<ImageView>(R.id.log_image)

            Glide.with(this)
                .load(travelogue.imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(imageView)
        }

    }
}