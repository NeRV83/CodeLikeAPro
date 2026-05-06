package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl

private val empty = Post()

class PostViewModel : ViewModel() {

    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.get()

    val editedNow = MutableLiveData(empty)
    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

    fun saveContent(content: String) {
        editedNow.value?.let {
            val trimmedContent = content.trim()
            if (it.content != trimmedContent) {
                repository.savePost(
                    it.copy(content = trimmedContent)
                )
            }
            editedNow.value = empty
        }
    }

    fun editContent(post: Post) {
        editedNow.value = post
    }

    fun cancelEdit() {
        editedNow.value = empty
    }

}