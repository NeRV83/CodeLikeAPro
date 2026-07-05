package ru.netology.nmedia.repository

import android.util.Log
import androidx.lifecycle.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
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

//    override fun getData(): List<Post> {
//        val request = Request.Builder()
//            .url("${BASE_URL}posts")
//            .build()
//
//        val response = client.newCall(request).execute()
//
//        return gson.fromJson(response.body.string(), postsType)
//    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        val request = Request.Builder()
            .url("${BASE_URL}posts")
            .build()

        client.newCall(request)
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val posts = response.body?.string() ?:throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(posts,postsType))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }

//    override fun shareById(id: Long) {
//        val post = getPostById(id)
//        val request = Request.Builder()
//            .url("${BASE_URL}posts/$id/shares")
//            .post("".toRequestBody(null))
//            .build()
//
//        val response = client.newCall(request).execute()
//
//        if (!response.isSuccessful) {
//            throw RuntimeException("Ошибка при добавлении шэра: ${response.code}")
//        }
//
//    }

    override fun shareByIdAsync(id: Long, callback: PostRepository.OperationCallback) {
        val request = Request.Builder()
            .url("${BASE_URL}posts/$id/shares")
            .post("".toRequestBody(null))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(Exception("HTTP error ${response.code}"))
                }
            }
        })
    }

//    override fun likeById(id: Long) {
//        val post = getPostById(id)
//        val request = if (post.likedByMe) {
//            Request.Builder()
//                .url("${BASE_URL}posts/$id/likes")
//                .delete()
//                .build()
//        } else {
//            Request.Builder()
//                .url("${BASE_URL}posts/$id/likes")
//                .post("".toRequestBody(null))
//                .build()
//        }
//
//        val response = client.newCall(request).execute()
//
//        if (!response.isSuccessful) {
//            throw RuntimeException("Ошибка при изменении лайка: ${response.code}")
//        }
//    }

    override fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: PostRepository.OperationCallback) {
        val request = if (likedByMe) {
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(Exception("HTTP error ${response.code}"))
                }
            }
        })
    }

//    override fun removeById(id: Long) {
//        val request = Request.Builder()
//            .url("${BASE_URL}posts/$id")
//            .delete()
//            .build()
//
//        val call = client.newCall(request)
//        call.execute()
//    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.OperationCallback) {
        val request = Request.Builder()
            .url("${BASE_URL}posts/$id")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(Exception("HTTP error ${response.code}"))
                }
            }
        })
    }

//    override fun savePost(post: Post): Post {
//        val request = Request.Builder()
//            .url("${BASE_URL}posts")
//            .post(gson.toJson(post).toRequestBody(JSON_type))
//            .build()
//
//        val call = client.newCall(request)
//        val response = call.execute()
//
//        return gson.fromJson(response.body.string(), Post::class.java)
//    }

    override fun savePostAsync(post: Post, callback: PostRepository.SaveCallback) {
        val request = Request.Builder()
            .url("${BASE_URL}posts")
            .post(gson.toJson(post).toRequestBody(JSON_type))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        callback.onError(Exception("HTTP error ${response.code}"))
                        return
                    }
                    val body = response.body?.string() ?: throw Exception("Response body is null")
                    val createdPost = gson.fromJson(body, Post::class.java)
                    callback.onSuccess(createdPost)
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }
        })
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