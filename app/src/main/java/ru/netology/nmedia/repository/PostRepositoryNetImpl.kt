package ru.netology.nmedia.repository

import okio.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post

class PostRepositoryNetImpl : PostRepository {

//    override fun getData(): List<Post> {
//        return PostApi.service.getAll()
//            .execute()
//            .body()
//            .orEmpty()
//    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        PostApi.service.getAll().enqueue(object : Callback<List<Post>> {

            override fun onResponse(
                call: Call<List<Post>>, response: Response<List<Post>>
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }
                val body = response.body() ?: run {
                    callback.onError(RuntimeException("body is null"))
                    return
                }
                callback.onSuccess(body)
            }

            override fun onFailure(
                call: Call<List<Post>>, t: Throwable
            ) {
                callback.onError(t)
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
        PostApi.service.shareById(id)
            .enqueue(object : Callback<Unit> {

                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    response.body() ?: run {
                        callback.onError(RuntimeException("body is null"))
                        return
                    }
                    callback.onSuccess()
                }


                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(t)
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

    override fun likeByIdAsync(
        id: Long, likedByMe: Boolean, callback: PostRepository.OperationCallback
    ) {
        if (!likedByMe) {
            PostApi.service.likeById(id)
                .enqueue(object : Callback<Unit> {

                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>
                    ) {
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        response.body() ?: run {
                            callback.onError(RuntimeException("body is null"))
                            return
                        }
                        callback.onSuccess()
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        callback.onError(t)
                    }
                })
        } else {
            PostApi.service.unlikeById(id)
                .enqueue(object : Callback<Unit> {

                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>
                    ) {
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        response.body() ?: run {
                            callback.onError(RuntimeException("body is null"))
                            return
                        }
                        callback.onSuccess()
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        callback.onError(t)
                    }
                })
        }

//        val request = if (likedByMe) {
//            Request.Builder().url("${BASE_URL}posts/$id/likes").delete().build()
//        } else {
//            Request.Builder().url("${BASE_URL}posts/$id/likes").post("".toRequestBody(null)).build()
//        }

    }

//    override fun removeById(id: Long) {
//        PostApi.service.removeById(id)
//            .execute()
//    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.OperationCallback) {
        PostApi.service.removeById(id).enqueue(object : Callback<Unit> {

            override fun onResponse(
                call: Call<Unit>, response: Response<Unit>
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }
                response.body() ?: run {
                    callback.onError(RuntimeException("body is null"))
                    return
                }
                callback.onSuccess()
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(t)
            }
        })
    }

//    override fun savePost(post: Post) {
//        PostApi.service.save(post)
//            .execute()
//    }

    override fun savePostAsync(post: Post, callback: PostRepository.SaveCallback) {
        PostApi.service.save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(
                    call: Call<Post?>,
                    response: Response<Post?>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    val body = response.body() ?: run {
                        callback.onError(RuntimeException("body is null"))
                        return
                    }
                    callback.onSuccess(body)
                }

                override fun onFailure(
                    call: Call<Post?>,
                    t: Throwable
                ) {
                    callback.onError(t)
                }
            })
    }
}