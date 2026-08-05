package ru.netology.nmedia.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PostRepositoryNetImpl(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map { entities ->
        entities.map(PostEntity::toDto)
    }

    private val _newCount = MutableStateFlow(0)
    override val newCount = _newCount.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            while (true) {
                delay(30_000) // каждые 30 секунд
                try {
                    val maxId = dao.getMaxId() ?: 0L
                    val response = PostApi.service.getNewer(maxId)
                    if (response.isSuccessful) {
                        val body = response.body() ?: emptyList()
                        if (body.isNotEmpty()) {
                            dao.insert(body.map { PostEntity.fromDto(it, isNew = true) })
                            _newCount.value = dao.getNewCount()
                        }
                    }
                } catch (e: IOException) {
                    // сетевые ошибки игнорируем, чтобы не прерывать цикл
                } catch (e: Exception) {
                    // другие ошибки логируем
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun getAll() {
        try {
            val response = PostApi.service.getAll()
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.map { PostEntity.fromDto(it, isNew = false) })
            _newCount.value = dao.getNewCount() // обновляем счётчик после загрузки
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        try {
            val response = PostApi.service.shareById(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body, isNew = false))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val postEntity = dao.getById(id) ?: return
            val response = if (postEntity.likedByMe) {
                PostApi.service.unlikeById(id)
            } else {
                PostApi.service.likeById(id)
            }
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body, isNew = false))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = PostApi.service.removeById(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun savePost(post: Post) {
        try {
            val response = PostApi.service.save(post)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body, isNew = false))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun markNewAsRead() {
        dao.markAllAsRead()
        _newCount.value = dao.getNewCount()
    }
}