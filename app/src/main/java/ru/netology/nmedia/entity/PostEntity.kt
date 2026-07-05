package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType

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
    val videoUrl: String? = null,
    val authorAvatar: String? = null,
    @Embedded
    var attachment: AttachmentEmbeddable?
) {
    fun toDto() = Post(id, author, content, published, likes, likedByMe, shares, views, videoUrl, authorAvatar, attachment?.toDto())

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
            dto.videoUrl,
            dto.authorAvatar,
            AttachmentEmbeddable.fromDto(dto.attachment)
        )
    }
}


data class AttachmentEmbeddable(
    var url: String,
    var description: String?,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, description, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.description, it.type)
        }
    }
}