package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long = 0,
    val author: String = "",
    val authorId: Long = 0,
    val content: String = "",
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val videoUrl: String? = null,
    val authorAvatar: String? = null,
    var attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
) : FeedItem

data class Ad(
    override val id: Long,
    val adImage: String,
) : FeedItem

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType,
)