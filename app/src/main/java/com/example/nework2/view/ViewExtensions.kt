package com.example.nework2.view

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.nework2.R


fun ImageView.load(url: String) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.icon_settings)
        .error(R.drawable.icon_error)
        .timeout(10_000)
        .circleCrop()
        .into(this)
}

fun ImageView.loadWithoutCircle(url: String) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.icon_settings)
        .error(R.drawable.icon_error)
        .timeout(10_000)
        .into(this)
}