package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>,
    ): MediatorResult = try {
        when (loadType) {
            LoadType.PREPEND -> prepend(state)
            LoadType.REFRESH -> refresh(state)
            LoadType.APPEND  -> append(state)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        MediatorResult.Error(e)
    }

    private suspend fun prepend(state: PagingState<Int, PostEntity>): MediatorResult {
        val topId = postRemoteKeyDao.max()
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        val response = service.getAfter(topId, state.config.pageSize)
        val body = response.requireBody()

        appDb.withTransaction {
            if (body.isNotEmpty()) {
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        PostRemoteKeyEntity.KeyType.AFTER,
                        body.first().id,
                    )
                )
            }
            postDao.insert(body.map(PostEntity::fromDto))
        }

        return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
    }

    private suspend fun refresh(state: PagingState<Int, PostEntity>): MediatorResult {
        val topId = postRemoteKeyDao.max()

        val response = if (topId == null) {
            service.getLatest(state.config.pageSize)
        } else {
            service.getAfter(topId, state.config.pageSize)
        }
        val body = response.requireBody()

        appDb.withTransaction {
            if (topId == null) {
                if (body.isNotEmpty()) {
                    postRemoteKeyDao.insert(
                        listOf(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id,
                            ),
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            ),
                        )
                    )
                }
            } else {
                if (body.isNotEmpty()) {
                    postRemoteKeyDao.insert(
                        PostRemoteKeyEntity(
                            PostRemoteKeyEntity.KeyType.AFTER,
                            body.first().id,
                        )
                    )
                }
            }
            postDao.insert(body.map(PostEntity::fromDto))
        }

        return MediatorResult.Success(endOfPaginationReached = false)
    }

    private suspend fun append(state: PagingState<Int, PostEntity>): MediatorResult {
        val bottomId = postRemoteKeyDao.min()
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        val response = service.getBefore(bottomId, state.config.pageSize)
        val body = response.requireBody()

        appDb.withTransaction {
            if (body.isNotEmpty()) {
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        PostRemoteKeyEntity.KeyType.BEFORE,
                        body.last().id,
                    )
                )
            }
            postDao.insert(body.map(PostEntity::fromDto))
        }

        return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
    }

    private fun Response<List<Post>>.requireBody(): List<Post> {
        if (!isSuccessful) throw ApiError(code(), message())
        return body() ?: throw ApiError(code(), message())
    }
}