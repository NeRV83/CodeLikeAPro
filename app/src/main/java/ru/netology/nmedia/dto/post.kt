package ru.netology.nmedia.dto

data class Post(
    val id: Long = 0,
    val author: String = "",
    val content: String = "",
    val published: String = "",
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0
)