package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
//    fun getData(): LiveData<List<Post>>
    fun getData(): List<Post>
    fun shareById(id: Long)
    fun likeById(id: Long)
    fun removeById(id: Long)
    fun savePost(post: Post): Post
}