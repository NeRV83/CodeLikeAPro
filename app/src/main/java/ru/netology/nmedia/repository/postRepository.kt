package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    //    fun getData(): LiveData<List<Post>>
    //fun getData(): List<Post>
//    fun shareById(id: Long)
//    fun likeById(id: Long)
    //fun removeById(id: Long)
    //fun savePost(post: Post)
    fun getAllAsync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Throwable)
    }

    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: OperationCallback)
    fun shareByIdAsync(id: Long, callback: OperationCallback)
    fun removeByIdAsync(id: Long, callback: OperationCallback)
    interface OperationCallback {
        fun onSuccess()
        fun onError(e: Throwable)
    }

    fun savePostAsync(post: Post, callback: SaveCallback)

    interface SaveCallback {
        fun onSuccess(post: Post)
        fun onError(e: Throwable)
    }


}