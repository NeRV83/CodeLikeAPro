package ru.netology.nmedia.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.Utility.formatShortNumber
import ru.netology.nmedia.util.Utility.formatTimestamp
import ru.netology.nmedia.util.Utility.getThumbnailDirectUrl
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onPostClick(post: Post) {}
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(
    PostDiffCallBack
) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            else -> R.layout.card_post
        }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (viewHolder as AdViewHolder).bind(item)
            is Post -> (viewHolder as PostViewHolder).bind(item)
            null -> Unit
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            else -> error("unknown view type ${viewType}")
        }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.adImage.load("${BuildConfig.BASE_URL}/images/${ad.adImage}")
    }
}

class PostViewHolder(
    private val binding: CardPostBinding, private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {

            author.text = post.author
            published.text = formatTimestamp(post.published)
            content.text = post.content

            share.text = formatShortNumber(post.shares)
            view.text = formatShortNumber(post.views)

            like.isChecked = post.likedByMe
            like.text = formatShortNumber(post.likes)

            if (post.videoUrl.isNullOrBlank()) {
                videoContainer.visibility = View.GONE
            } else {
                videoContainer.visibility = View.VISIBLE

                videoThumbnail.load(getThumbnailDirectUrl(post.videoUrl))

                playButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, post.videoUrl.toUri())
                    root.context.startActivity(intent)
                }
            }

            if (post.attachment?.url.isNullOrBlank()) {
                imgContainer.visibility = View.GONE
            } else {
                imgContainer.visibility = View.VISIBLE
                imgThumbnail.load("${BuildConfig.BASE_URL}/images/${post.attachment?.url}")
            }

            avatar.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")

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
}

object PostDiffCallBack : DiffUtil.ItemCallback<FeedItem>() {
    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem == newItem

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return if (oldItem::class != newItem::class) {
            false
        } else {
            oldItem.id == newItem.id
        }
    }
}