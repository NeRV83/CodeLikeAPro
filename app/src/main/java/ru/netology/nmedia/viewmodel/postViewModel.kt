package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = "",
    authorId = 0,
    content = "",
    published = 0,
    likes = 0,
    likedByMe = false,
    videoUrl = null,
    authorAvatar = null,
    attachment = null,
    ownedByMe = false,
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val auth: AppAuth,
) : ViewModel() {

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState> = _state

    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = post.authorId == myId)
                }
            }
        }

    val newCount = repository.newCount.asLiveData()

    val editedNow = MutableLiveData(empty)

    private val _postId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val post: Flow<Post?> = _postId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getPostFlow(id)
        }

    fun setPostId(id: Long) {
        _postId.value = id
        viewModelScope.launch {
            try {
                repository.fetchPost(id)
            } catch (_: Exception) {
            }
        }
    }

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    val refreshTrigger: SharedFlow<Unit> = _refreshTrigger.asSharedFlow()

    init {
        viewModelScope.launch {
            auth.authStateFlow
                .drop(1)
                .collect {
                    _refreshTrigger.tryEmit(Unit)
                }
        }
    }

    fun markNewAsRead() {
        viewModelScope.launch { repository.markNewAsRead() }
    }

    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeById(id)
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
                _refreshTrigger.tryEmit(Unit)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun editContent(post: Post) {
        editedNow.value = post
    }
}