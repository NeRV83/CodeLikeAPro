package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.R

typealias OnLikeListener = (post: Post) -> Unit
typealias OnShareListener = (post: Post) -> Unit

class PostAdapter(
    private val onLikeListener: OnLikeListener,
    private val onShareListener: OnShareListener
) :
    ListAdapter<Post, PostViewHolder>(
        PostDiffCallBack
    ) {

    override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int) {
        val post = getItem(position)
        viewHolder.bind(post)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onLikeListener, onShareListener)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onLikeListener: OnLikeListener,
    private val onShareListener: OnShareListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = formatShortNumber(post.likes)
            shareCount.text = formatShortNumber(post.shares)
            viewCount.text = formatShortNumber(post.views)
            like.setImageResource(
                if (post.isLiked) ru.netology.nmedia.R.drawable.ic_liked_24 else ru.netology.nmedia.R.drawable.ic_likes_24
            )
            like.setOnClickListener {
                onLikeListener(post)
            }
            share.setOnClickListener {
                onShareListener(post)
            }
        }
    }

    fun formatShortNumber(number: Int): String {
        return when {
            number < 1000 -> number.toString()
            number < 10_000 -> {
                val thousands = number / 1000.0
                // Округляем до 1 знака после запятой (десятки тысяч)
                val rounded = (thousands * 10).toInt() / 10.0
                "${rounded}K".replace(".0K", "K")
            }

            number < 1_000_000 -> {
                val thousands = number / 1000
                "${thousands}K"
            }

            number < 10_000_000 -> {
                val millions = number / 1_000_000.0
                val rounded = (millions * 10).toInt() / 10.0
                "${rounded}M".replace(".0M", "M")
            }

            else -> {
                val millions = number / 1_000_000
                "${millions}M"
            }
        }
    }
}

object PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
    override fun areContentsTheSame(p0: Post, p1: Post) = p0 == p1

    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
}