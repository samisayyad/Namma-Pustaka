package com.nammapustaka.utils

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.nammapustaka.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.animateIn() {
    val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
    startAnimation(anim)
    show()
}

fun View.animateOut() {
    val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
    startAnimation(anim)
    hide()
}

fun View.slideUpIn() {
    translationY = height.toFloat()
    alpha = 0f
    animate()
        .translationY(0f)
        .alpha(1f)
        .setDuration(380)
        .setInterpolator(android.view.animation.DecelerateInterpolator(2f))
        .start()
}

fun ImageView.loadBookCover(url: String?) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.bg_book_cover_placeholder)
        .error(R.drawable.bg_book_cover_placeholder)
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .into(this)
}

fun ImageView.loadAvatar(url: String?) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_avatar_placeholder)
        .error(R.drawable.ic_avatar_placeholder)
        .circleCrop()
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .into(this)
}

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Long.toFormattedDate(): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))
}

fun Long.toDaysRemaining(): Int {
    val now = System.currentTimeMillis()
    val diff = this - now
    return TimeUnit.MILLISECONDS.toDays(diff).toInt()
}

fun Long.isOverdue(): Boolean = System.currentTimeMillis() > this

fun String.toGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
