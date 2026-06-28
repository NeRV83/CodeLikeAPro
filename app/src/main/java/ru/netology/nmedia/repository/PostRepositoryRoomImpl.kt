package ru.netology.nmedia.repository
//
//import androidx.lifecycle.map
//import ru.netology.nmedia.dao.PostDao
//import ru.netology.nmedia.dto.Post
//import ru.netology.nmedia.entity.PostEntity
//import kotlin.collections.map
//
//class PostRepositoryRoomImpl(
//    private val dao: PostDao,
//) : PostRepository {
//
//    override fun getData() = dao.getData().map { list ->
//        list.map {
//            it.toDto()
//        }
//    }
//
//    override fun likeById(id: Long) {
//        dao.likeById(id)
//    }
//
//    override fun savePost(post: Post) {
//        dao.savePost(PostEntity.fromDto(post))
//    }
//
//    override fun removeById(id: Long) {
//        dao.removeById(id)
//    }
//
//    override fun shareById(id: Long) {
//        dao.shareById(id)
//    }
//}