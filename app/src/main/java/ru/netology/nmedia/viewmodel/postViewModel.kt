package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = 0,
    likes = 0,
    likedByMe = false,
    videoUrl = null,
    authorAvatar = null,
    attachment = null
)

@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {


    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState> = _state

    val data = repository.data.map {
        FeedModel(it, it.isEmpty())
    }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    val newCount = repository.newCount.asLiveData() 

    val editedNow = MutableLiveData(empty)

    fun markNewAsRead() {
        viewModelScope.launch {
            repository.markNewAsRead()
        }
    }

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    init {
        loadPosts()
    }

    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeById(id)
                // LiveData обновится автоматически
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun shareById(id: Long) {
        viewModelScope.launch {
            try {
                repository.shareById(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun saveContent(content: String, videoUrl: String) {
        viewModelScope.launch {
            val post = editedNow.value ?: return@launch
            val trimmedContent = content.trim()
            val video = videoUrl.ifBlank { null }

            if (post.content == trimmedContent && post.videoUrl == video) {
                return@launch
            }

            val newPost = post.copy(
                author = "Me",
                content = trimmedContent,
                videoUrl = video,
                authorAvatar = "netology.jpg"
            )

            try {
                repository.savePost(newPost)
                _postCreated.value = Unit
                editedNow.value = empty
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun editContent(post: Post) {
        editedNow.value = post
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }
}