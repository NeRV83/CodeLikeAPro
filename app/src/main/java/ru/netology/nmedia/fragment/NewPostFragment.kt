package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        viewModel.editedNow.observe(viewLifecycleOwner) { post ->
            if (post.id != 0L) {
                binding.edit.setText(post.content)
                binding.videoInput.setText(post.video ?: "")
                binding.edit.setSelection(post.content.length)
            } else {
                binding.edit.setText("")
                binding.videoInput.setText("")
            }
        }

        arguments?.textArg?.let { text ->
            binding.edit.setText(text)
            binding.edit.setSelection(text.length)
        }

        AndroidUtils.showKeyboard(binding.edit)

        binding.ok.setOnClickListener {
            val content = binding.edit.text.toString().trim()
            val videoUrl = binding.videoInput.text.toString().trim()

            if (content.isBlank()) {
                binding.edit.error = getString(R.string.error_empty_content)
                return@setOnClickListener
            }

            viewModel.saveContent(content, videoUrl)
            findNavController().navigateUp()
        }

        return binding.root
    }
}