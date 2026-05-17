package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryRoomImpl

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likes = 0,
    isLiked = false,
    videoUrl = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository =
        PostRepositoryRoomImpl(AppDb.getInstance(application).postDao)

    val data = repository.getData()
    val editedNow = MutableLiveData(empty)

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

    fun saveContent(content: String, videoUrl: String) {
        editedNow.value?.let {
            val trimmedContent = content.trim()
            val videoUrl = videoUrl.ifBlank { null }

            if (it.content != trimmedContent) {
                repository.savePost(
                    it.copy(content = trimmedContent, videoUrl = videoUrl)
                )
            }
            editedNow.value = empty
        }
    }

    fun editContent(post: Post) {
        editedNow.value = post
    }
}