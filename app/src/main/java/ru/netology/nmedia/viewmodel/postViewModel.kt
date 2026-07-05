package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0, author = "", content = "", published = 0, likes = 0, likedByMe = false, videoUrl = null, authorAvatar = null, attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

//    private val repository: PostRepository =
//        PostRepositoryRoomImpl(AppDb.getInstance(application).postDao)

    private val repository: PostRepository = PostRepositoryNetImpl()

    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val editedNow = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPostsAsync()
    }

//    fun likeById(id: Long) {
//        thread {
//            try {
//                repository.likeById(id)
//                loadPostsAsync()
//            } catch (e: Exception) {
//                _data.postValue(FeedModel(error = true))
//            }
//        }
//    }

    fun likeById(id: Long) {
        val likedByMe = data.value?.posts?.find { it.id == id }?.likedByMe ?: return

        repository.likeByIdAsync(id, likedByMe, object : PostRepository.OperationCallback {
            override fun onSuccess() {
                loadPostsAsync()
            }

            override fun onError(e: Throwable) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

//    fun shareById(id: Long) {
//        thread {
//            try {
//                repository.shareById(id)
//                loadPostsAsync()
//            } catch (e: Exception) {
//                _data.postValue(FeedModel(error = true))
//            }
//        }
//    }

    fun shareById(id: Long) {
        repository.shareByIdAsync(id, object : PostRepository.OperationCallback {
            override fun onSuccess() {
                loadPostsAsync()
            }

            override fun onError(e: Throwable) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

//    fun removeById(id: Long) {
//        thread {
//            try {
//                repository.removeById(id)
//                loadPostsAsync()
//            } catch (e: Exception) {
//                _data.postValue(FeedModel(error = true))
//            }
//        }
//
//    }

    fun removeById(id: Long) {
        repository.removeByIdAsync(id, object : PostRepository.OperationCallback {
            override fun onSuccess() {
                loadPostsAsync()
            }

            override fun onError(e: Throwable) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

//    fun saveContent(content: String, videoUrl: String) {
//
//
//        thread {
//            try {
//                editedNow.value?.let {
//                    val trimmedContent = content.trim()
//                    val videoUrl = videoUrl.ifBlank { null }
//
//                    if (it.content != trimmedContent) {
//                        repository.savePost(
//                            it.copy(author = "Me", content = trimmedContent, videoUrl = videoUrl)
//                        )
//
//                        _postCreated.postValue(Unit)
//                        editedNow.postValue(empty)
//                    }
//
//                }
//            } catch (e: Exception) {
//                _data.postValue(FeedModel(error = true))
//            }
//        }
//    }

    fun saveContent(content: String, videoUrl: String) {
        val post = editedNow.value ?: return
        val trimmedContent = content.trim()
        val video = videoUrl.ifBlank { null }

        if (post.content == trimmedContent && post.videoUrl == video) {
            return
        }

        val newPost = post.copy(
            author = "Me",
            content = trimmedContent,
            videoUrl = video,
            authorAvatar = "netology.jpg"
        )

        repository.savePostAsync(newPost, object : PostRepository.SaveCallback {
            override fun onSuccess(createdPost: Post) {
                _postCreated.postValue(Unit)
                editedNow.postValue(empty)
                loadPostsAsync()
            }

            override fun onError(e: Throwable) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun editContent(post: Post) {
        editedNow.value = post
    }

//    fun loadPosts() {
//        thread {
//            _data.postValue(FeedModel(loading = true))
//
//            _data.postValue(
//                try {
//                    val posts = repository.getData()
//                    FeedModel(posts = posts, empty = posts.isEmpty())
//                } catch (_: Exception) {
//                    FeedModel(error = true)
//                }
//            )
//        }
//    }

    fun loadPostsAsync() {
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Throwable) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }
}