package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.io.IOException

class PostRepositoryNetImpl(private val dao: PostDao) : PostRepository {

    override val data: LiveData<List<Post>> = dao.getAll().map { it.map(PostEntity::toDto) }

    override suspend fun getAll() {
        val posts: List<Post> = PostApi.service.getAll()

        dao.insert(posts.map(PostEntity::fromDto))
    }

    override suspend fun shareById(id: Long) {

        val updatedPost = PostApi.service.shareById(id)
        dao.insert(PostEntity.fromDto(updatedPost))
    }

    override suspend fun likeById(id: Long) {
        val postEntity = dao.getById(id) ?: return
        val updatedPost = if (postEntity.likedByMe) {
            PostApi.service.unlikeById(id)
        } else {
            PostApi.service.likeById(id)
        }
        dao.insert(PostEntity.fromDto(updatedPost))
    }

    override suspend fun removeById(id: Long) {
        PostApi.service.removeById(id)
        dao.removeById(id)
    }

    override suspend fun savePost(post: Post) {
        val updatedPost = PostApi.service.save(post)
        dao.insert(PostEntity.fromDto(updatedPost))
    }
}