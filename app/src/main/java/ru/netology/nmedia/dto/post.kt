package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long = 0,
    val author: String = "",
    val content: String = "",
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val videoUrl: String? = null,
    val authorAvatar: String? = null,
    var attachment: Attachment? = null
)

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType,
)