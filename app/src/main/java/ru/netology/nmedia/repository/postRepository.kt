package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    val newCount: Flow<Int>

    suspend fun getAll()
    suspend fun shareById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun savePost(post: Post)
    suspend fun markNewAsRead()
    fun getNewerCount(id: Long): Flow<Int>

    fun getPostFlow(id: Long): Flow<Post?>

    suspend fun fetchPost(id: Long)
}