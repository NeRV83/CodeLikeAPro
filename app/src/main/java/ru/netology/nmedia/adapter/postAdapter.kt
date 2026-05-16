package ru.netology.nmedia.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onPostClick(post: Post) {}
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(
    PostDiffCallBack
) {

    override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int) {
        val post = getItem(position)
        viewHolder.bind(post)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding, private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content

            share.text = formatShortNumber(post.shares)
            view.text = formatShortNumber(post.views)

            like.isChecked = post.isLiked
            like.text = formatShortNumber(post.likes)

            if (post.video.isNullOrBlank()) {
                videoContainer.visibility = android.view.View.GONE
            } else {
                videoContainer.visibility = android.view.View.VISIBLE

                playButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, post.video.toUri())
                    root.context.startActivity(intent)
                }
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
            root.setOnClickListener {
                onInteractionListener.onPostClick(post)
            }
        }
    }

    fun formatShortNumber(number: Int): String {
        return when {
            number < 1000 -> number.toString()
            number < 10_000 -> {
                val thousands = number / 1000.0
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