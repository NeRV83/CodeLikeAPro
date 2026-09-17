package ru.netology.nmedia.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindsPostRepository(impl: PostRepositoryNetImpl): PostRepository
}