package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PostRepositoryFileImpl(private val context: Context) : PostRepository {

    private var nextId = 1L
    private var posts = emptyList<Post>()
        set(value) {
            field = value
            sync()
        }
    private val data = MutableLiveData(posts)

    init {
        val file = context.filesDir.resolve(FILENAME)
        if (file.exists()) {
            context.openFileInput(FILENAME).bufferedReader().use {
                posts = gson.fromJson(it, type)
                nextId = (posts.maxOfOrNull { it.id } ?: 0) + 1
                data.value = posts
            }
        }
    }

    override fun get(): LiveData<List<Post>> = data

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(shares = it.shares + 1)
        }
        data.value = posts
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
                if (it.id != post.id) it else it.copy(content = post.content, video = post.video)
            }
        }

        data.value = posts
        return
    }

    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale("ru"))
        return currentDate.format(formatter).toString()
    }

    private fun sync() {
        context.openFileOutput(FILENAME, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }

    companion object {
        private const val FILENAME = "posts.json"

        private val gson = Gson()
        private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    }
}