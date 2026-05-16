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

        val post = viewModel.data.value?.find { it.id == postId }

        if (post == null) {
            findNavController().navigateUp()
            return
        }

        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content

            share.text = formatShortNumber(post.shares)
            view1.text = formatShortNumber(post.views)

            like.isChecked = post.isLiked
            like.text = formatShortNumber(post.likes)

            if (post.video.isNullOrBlank()) {
                videoContainer.visibility = View.GONE
            } else {
                videoContainer.visibility = View.VISIBLE
                playButton.setOnClickListener {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(post.video)
                    )
                    startActivity(intent)
                }
            }

            like.setOnClickListener {
                viewModel.likeById(post.id)
                viewModel.data.observe(viewLifecycleOwner) { posts ->
                    val updatedPost = posts.find { it.id == post.id }
                    updatedPost?.let {
                        like.isChecked = it.isLiked
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

    private fun formatShortNumber(number: Int): String {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}