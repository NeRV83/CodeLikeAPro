package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import androidx.core.net.toUri
import ru.netology.nmedia.util.Utility.formatShortNumber
import ru.netology.nmedia.util.Utility.formatTimestamp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.netology.nmedia.util.Utility.getThumbnailDirectUrl


class PostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = arguments?.getLong("postId", 0) ?: 0

        if (postId == 0L) {
            findNavController().navigateUp()
            return
        }

//        val post = viewModel.data.value?.find { it.id == postId }
        val post = viewModel.data.value?.posts?.find { it.id == postId }

        if (post == null) {
            findNavController().navigateUp()
            return
        }

        binding.apply {
            author.text = post.author
            published.text = formatTimestamp(post.published)
            content.text = post.content

            val url = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
            Glide.with(binding.avatar)
                .load(url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .circleCrop()
                .into(binding.avatar)

            share.text = formatShortNumber(post.shares)
            view1.text = formatShortNumber(post.views)

            like.isChecked = post.likedByMe
            like.text = formatShortNumber(post.likes)

            if (post.videoUrl.isNullOrBlank()) {
                videoContainer.visibility = View.GONE
            } else {
                videoContainer.visibility = View.VISIBLE

                val thumbnailDirectUrl = getThumbnailDirectUrl(post.videoUrl)
                Glide.with(binding.videoThumbnail)
                    .load(thumbnailDirectUrl)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(binding.videoThumbnail)

                playButton.setOnClickListener {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        post.videoUrl.toUri()
                    )
                    startActivity(intent)
                }
            }

            if (post.attachment?.url.isNullOrBlank()) {
                imgContainer.visibility = View.GONE
            } else {
                imgContainer.visibility = View.VISIBLE
                val url = "http://10.0.2.2:9999/images/${post.attachment?.url}"
                Glide.with(binding.imgContainer)
                    .load(url)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(binding.imgThumbnail)
            }




            like.setOnClickListener {
                viewModel.likeById(post.id)
//                viewModel.data.observe(viewLifecycleOwner) { posts ->
                viewModel.data.observe(viewLifecycleOwner) { feedModel ->
//                    val updatedPost = posts.find { it.id == post.id }
                    val updatedPost = feedModel.posts.find { it.id == post.id }
                    updatedPost?.let {
                        like.isChecked = it.likedByMe
                        like.text = formatShortNumber(it.likes)
                        share.text = formatShortNumber(it.shares)
                    }
                }
            }

            share.setOnClickListener {
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, post.content)
                }
                val chooserIntent = android.content.Intent.createChooser(
                    shareIntent,
                    getString(R.string.chooser_share_post)
                )
                startActivity(chooserIntent)
                viewModel.shareById(post.id)
            }

            menu.setOnClickListener {
                androidx.appcompat.widget.PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                viewModel.editContent(post)
                                findNavController().navigate(
                                    R.id.action_postFragment_to_newPostFragment
                                )
                                true
                            }

                            R.id.remove -> {
                                viewModel.removeById(post.id)
                                findNavController().navigateUp()
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}