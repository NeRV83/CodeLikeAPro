package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: Long = 0,
    val likes: Int = 0,
    val likedByMe: Boolean,
    val shares: Int = 0,
    val views: Int = 0,
    val videoUrl: String? = null
) {
    fun toDto() = Post(id, author, content, published, likes, likedByMe, shares, views, videoUrl)

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            dto.id,
            dto.author,
            dto.content,
            dto.published,
            dto.likes,
            dto.likedByMe,
            dto.shares,
            dto.views,
            dto.videoUrl
        )
    }
}