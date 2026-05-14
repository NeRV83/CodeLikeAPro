package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

data class PostEditingData(
    val content: String = "", val video: String = ""
)

object NewPostResultContract : ActivityResultContract<Pair<String?, String?>, PostEditingData?>() {

    override fun createIntent(context: Context, input: Pair<String?, String?>): Intent =
        Intent(context, NewPostActivity::class.java).apply {
            putExtra(Intent.EXTRA_TEXT, input.first)
            putExtra("video", input.second)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): PostEditingData? {
        return if (resultCode == Activity.RESULT_OK) {
            PostEditingData(
                content = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: return null,
                video = intent?.getStringExtra("video") ?: ""
            )
        } else {
            null
        }
    }
}