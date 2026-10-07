package ru.netology.nmedia.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.Utility.formatShortNumber
import ru.netology.nmedia.util.Utility.formatTimestamp
import ru.netology.nmedia.util.Utility.getThumbnailDirectUrl
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class PostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
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

        viewModel.setPostId(postId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.post.collectLatest { post ->
                    if (post == null) {
                        findNavController().navigateUp()
                        return@collectLatest
                    }
                    bind(post)
                }
            }
        }
    }

    private fun bind(post: Post) = binding.apply {
        author.text = post.author
        published.text = formatTimestamp(post.published)
        content.text = post.content

        avatar.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")

        share.text = formatShortNumber(post.shares)
        view1.text = formatShortNumber(post.views)

        like.isChecked = post.likedByMe
        like.text = formatShortNumber(post.likes)

        if (post.videoUrl.isNullOrBlank()) {
            videoContainer.visibility = View.GONE
        } else {
            videoContainer.visibility = View.VISIBLE

            videoThumbnail.load(getThumbnailDirectUrl(post.videoUrl))

            playButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, post.videoUrl.toUri()))
            }
        }

        if (post.attachment?.url.isNullOrBlank()) {
            imgContainer.visibility = View.GONE
        } else {
            imgContainer.visibility = View.VISIBLE
            imgThumbnail.load("${BuildConfig.BASE_URL}/images/${post.attachment?.url}")
        }

        like.setOnClickListener {
            viewModel.likeById(post.id)
        }

        share.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, post.content)
            }
            val chooserIntent = Intent.createChooser(
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}