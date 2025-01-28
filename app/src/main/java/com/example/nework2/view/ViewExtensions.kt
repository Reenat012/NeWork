package com.example.nework2.view

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.nework2.R


fun ImageView.loadAvatar(url: String?) {
    Glide.with(this)
        .load(url)
        .error(R.drawable.users)
        .placeholder(R.drawable.users)
        .timeout(10_000)
        .circleCrop()
        .into(this)
}

fun ImageView.load(url: String) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.users)
        .error(R.drawable.icon_error)
        .timeout(10_000)
        .circleCrop()
        .into(this)
}

fun ImageView.loadWithoutCircle(url: String) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.users)
        .error(R.drawable.icon_error)
        .timeout(10_000)
        .into(this)
}

fun ImageView.loadAttachment(url: String?) {
    if (url == null) {
        return
    }
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.users)
        .error(R.drawable.icon_error)
        .timeout(10_000)
        .into(this)
}