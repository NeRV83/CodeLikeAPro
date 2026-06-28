package ru.netology.nmedia.repository

import android.util.Log
import androidx.lifecycle.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.util.concurrent.TimeUnit
import kotlin.collections.map

class PostRepositoryNetImpl : PostRepository {

    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999/api/slow/"
        val JSON_type = "application/json".toMediaType()
    }

    private val client = okhttp3.OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val postsType = object : TypeToken<List<Post>>() {}.type

    override fun getData(): List<Post> {
        val request = Request.Builder()
            .url("${BASE_URL}posts")
            .build()

        val response = client.newCall(request).execute()

        return gson.fromJson(response.body.string(), postsType)
    }

    override fun shareById(id: Long) {
        val post = getPostById(id)
        val request = Request.Builder()
            .url("${BASE_URL}posts/$id/shares")
            .post("".toRequestBody(null))
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Ошибка при добавлении шэра: ${response.code}")
        }

    }

    override fun likeById(id: Long) {
        val post = getPostById(id)
        val request = if (post.likedByMe) {
            Request.Builder()
                .url("${BASE_URL}posts/$id/likes")
                .delete()
                .build()
        } else {
            Request.Builder()
                .url("${BASE_URL}posts/$id/likes")
                .post("".toRequestBody(null))
                .build()
        }

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Ошибка при изменении лайка: ${response.code}")
        }
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .url("${BASE_URL}posts/$id")
            .delete()
            .build()

        val call = client.newCall(request)
        call.execute()
    }

    override fun savePost(post: Post): Post {
        val request = Request.Builder()
            .url("${BASE_URL}posts")
            .post(gson.toJson(post).toRequestBody(JSON_type))
            .build()

        val call = client.newCall(request)
        val response = call.execute()

        return gson.fromJson(response.body.string(), Post::class.java)
    }

    private fun getPostById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}posts/$id")
            .build()

        val call = client.newCall(request)
        val response = call.execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Ошибка получения поста: ${response.code}")
        }

        return gson.fromJson(response.body.string(), Post::class.java)
    }

}