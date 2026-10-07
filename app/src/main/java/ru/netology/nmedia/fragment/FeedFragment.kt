package ru.netology.nmedia.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        binding.swipeRefreshLayout.setColorSchemeColors(Color.TRANSPARENT)
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT)

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.editContent(post)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
                viewModel.shareById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onPostClick(post: Post) {
                val bundle = Bundle().apply {
                    putLong("postId", post.id)
                }
                findNavController().navigate(R.id.action_feedFragment_to_postFragment, bundle)
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {
                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefreshLayout.isRefreshing = state.refresh is LoadState.Loading

                    val isEmpty = state.refresh is LoadState.NotLoading && adapter.itemCount == 0
                    binding.empty.isVisible = isEmpty
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshTrigger.collect {
                    adapter.refresh()
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.errorGroup.isVisible = state.error
            //binding.loadingProgress.isVisible = state.loading

//            if ((!state.loading || state.error) && binding.swipeRefreshLayout.isRefreshing) {
//                binding.swipeRefreshLayout.isRefreshing = false
//            }
        }

        viewModel.newCount.observe(viewLifecycleOwner) { count ->
            binding.newPostsBanner.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.newPostsText.text = getString(R.string.new_posts_banner, count)
        }

        binding.newPostsBanner.setOnClickListener {
            viewModel.markNewAsRead()
            viewLifecycleOwner.lifecycleScope.launch {
//                adapter.refresh()
            }
            binding.list.postDelayed({
                binding.list.smoothScrollToPosition(0)
            }, 200)
        }

        binding.retry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                adapter.retry()
            }
        }

        binding.add.setOnClickListener {
            viewModel.editContent(
                Post(
                    id = 0,
                    author = "",
                    authorId = 0,
                    content = "",
                    published = 0,
                    likes = 0,
                    likedByMe = false,
                    videoUrl = null,
                    shares = 0,
                    views = 0,
                    authorAvatar = null,
                    attachment = null,
                    ownedByMe = false,
                )
            )
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}