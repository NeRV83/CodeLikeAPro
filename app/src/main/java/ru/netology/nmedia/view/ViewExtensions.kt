package ru.netology.nmedia.view

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nmedia.R

private const val TIMEOUT_MS = 10_000

fun ImageView.load(
    url: String?,
    vararg transforms: BitmapTransformation,
) = Glide.with(this)
    .load(url)
    .placeholder(R.drawable.ic_loading_100dp)
    .error(R.drawable.ic_error_100dp)
    .timeout(TIMEOUT_MS)
    .transform(*transforms)
    .into(this)

fun ImageView.loadCircleCrop(
    url: String?,
    vararg transforms: BitmapTransformation,
) = load(url, CircleCrop(), *transforms)