package ru.netology.nmedia.api

import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.BuildConfig
import java.util.concurrent.TimeUnit

const val BASE_URL = "${BuildConfig.BASE_URL}"

private val client = okhttp3.OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    })
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .baseUrl(BASE_URL)
    .build()

interface PostApiService {
    @GET("posts")
    fun getAll(): Call<List<Post>>

    @GET("posts/{id}")
    fun getById(@Path("id") id: Long): Call<Post>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

    @DELETE("posts/{id}")
    fun removeById(@Path("id") id: Long): Call<Unit>

    @POST("posts/{id}/shares")
    fun shareById(@Path("id") id: Long): Call<Unit>

    @POST("posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Call<Unit>

    @DELETE("posts/{id}/likes")
    fun unlikeById(@Path("id") id: Long): Call<Unit>

}

object PostApi {
    val service: PostApiService by lazy {
        retrofit.create()
    }
}