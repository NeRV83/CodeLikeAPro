package ru.netology.nmedia.service

enum class Action {
    LIKE,
    NEW_POST,
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

data class NewPost(
    val userName: String,
    val postId: Long,
    val content: String,
)
