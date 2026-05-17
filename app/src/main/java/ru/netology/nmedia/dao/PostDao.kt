package ru.netology.nmedia.dao

import ru.netology.nmedia.dto.Post

interface PostDao {
    fun getData(): List<Post>
    fun savePost(post: Post): Post
    fun likeById(id: Long)
    fun removeById(id: Long)
    fun shareById(id: Long)
}