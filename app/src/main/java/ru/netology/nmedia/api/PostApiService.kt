package ru.netology.nmedia.api

import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
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
    suspend fun getAll(): List<Post>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Post

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long)

    @POST("posts/{id}/shares")
    suspend fun shareById(@Path("id") id: Long): Post

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Post

}

object PostApi {
    val service: PostApiService by lazy {
        retrofit.create(PostApiService::class.java)
    }
}