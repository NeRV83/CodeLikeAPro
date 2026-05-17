package ru.netology.nmedia.repository

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.Utility.getCurrentDate

class PostRepositorySharedPrefsImpl(
    context: Context,
) : PostRepository {

    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private var nextId = 1L
    private var posts = emptyList<Post>()
        set(value) {
            field = value
            sync()
        }
    private val data = MutableLiveData(posts)

    init {
        prefs.getString(KEY_POSTS, null)?.let { value ->
            posts = gson.fromJson(value, type)
            nextId = (posts.maxOfOrNull { it.id } ?: 0) + 1
            data.value = posts
        }
    }

    override fun getData(): LiveData<List<Post>> = data

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(shares = it.shares + 1)
        }
        data.value = posts
    }

    override fun savePost(post: Post) {
        if (post.id == 0L) {
            // TODO: remove hardcoded author
            posts = listOf(
                post.copy(
                    id = nextId++,
                    author = "Me",
                    published = getCurrentDate()
                )
            ) + posts

        } else {
            posts = posts.map {
                if (it.id != post.id) it else it.copy(content = post.content, videoUrl = post.videoUrl)
            }
        }

        data.value = posts
        return
    }

    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                isLiked = !it.isLiked,
                likes = if (it.isLiked) it.likes - 1 else it.likes + 1
            )
        }
        data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    private fun sync() {
        prefs.edit {
            putString(KEY_POSTS, gson.toJson(posts))
        }
    }

    companion object {
        private const val KEY_POSTS = "posts"

        private val gson = Gson()
        private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    }
}