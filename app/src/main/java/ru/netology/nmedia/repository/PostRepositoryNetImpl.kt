package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class PostRepositoryNetImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            dao.getPagingSource()
        },
        remoteMediator = PostRemoteMediator(
            service = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map {
            it.map(PostEntity::toDto)
                .insertSeparators { previousItem, _ ->
                    if (previousItem?.id?.rem(5) == 0L) {
                        Ad(Random.nextLong(), "figma.jpg")
                    } else {
                        null
                    }
                }
        }

//    private val _newCount = MutableStateFlow(0)
//    override val newCount = _newCount.asStateFlow()

    override val newCount: Flow<Int> = dao.getNewCountFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            while (true) {
                try {
                    val maxId = dao.getMaxId() ?: 0L
                    val response = apiService.getNewer(maxId)
                    if (response.isSuccessful) {
                        val body = response.body() ?: emptyList()
                        if (body.isNotEmpty()) {
                            dao.insert(body.map { PostEntity.fromDto(it, isNew = true) })
//                            _newCount.value = dao.getNewCount()
                        }
                    }
                } catch (e: IOException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30_000.milliseconds)
            }
        }
    }

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.map { PostEntity.fromDto(it, isNew = false) })
//            _newCount.value = dao.getNewCount()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        try {
            val response = apiService.shareById(id)
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
        val original = dao.getById(id) ?: return

        // 1. Сразу обновляем БД — UI отреагирует мгновенно
        dao.likeById(id)

        try {
            val response = if (original.likedByMe) {
                apiService.unlikeById(id)
            } else {
                apiService.likeById(id)
            }
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            // 2. Заменяем оптимистичное значение на серверное
            dao.insert(PostEntity.fromDto(body, isNew = false))
        } catch (e: IOException) {
            // 3. Откат: возвращаем исходное состояние
            dao.insert(original)
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(original)
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeById(id)
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
            val response = apiService.save(post)
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
//        _newCount.value = dao.getNewCount()
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        var lastId = id
        while (true) {
            delay(120_000L)
            val response = apiService.getNewer(lastId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            if (body.isNotEmpty()) {
                dao.insert(body.toEntity(isNew = true))
                lastId = body.maxOf { it.id }
            }
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override fun getPostFlow(id: Long): Flow<Post?> =
        dao.getByIdFlow(id).map { it?.toDto() }

    override suspend fun fetchPost(id: Long) {
        if (dao.getById(id) != null) return
        try {
            val response = apiService.getById(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body, isNew = false))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}