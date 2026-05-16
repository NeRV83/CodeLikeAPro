package ru.netology.nmedia.repository

import android.content.Context
import androidx.core.content.edit

class DraftRepository(context: Context) {
    private val prefs = context.getSharedPreferences("draft", Context.MODE_PRIVATE)

    fun setDraft(content: String, videoUrl: String) {
        prefs.edit {
            putString(KEY_DRAFT_CONTENT, content)
            putString(KEY_DRAFT_VIDEO_URL, videoUrl)
        }
    }

    fun getDraft(): Pair<String, String> {
        val content = prefs.getString(KEY_DRAFT_CONTENT, "") ?: ""
        val videoUrl = prefs.getString(KEY_DRAFT_VIDEO_URL, "") ?: ""
        return Pair(content, videoUrl)
    }

    fun clearDraft() {
        prefs.edit {
            remove(KEY_DRAFT_CONTENT)
            remove(KEY_DRAFT_VIDEO_URL)
        }
    }

    fun hasDraft(): Boolean {
        val draftPair = getDraft()
        return draftPair.first.isNotBlank() || draftPair.second.isNotBlank()
    }

    companion object {
        private const val KEY_DRAFT_CONTENT = "draft_content"
        private const val KEY_DRAFT_VIDEO_URL = "draft_video_url"
    }
}