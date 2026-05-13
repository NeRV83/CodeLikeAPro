package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import ru.netology.nmedia.R
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityNewPostBinding
import ru.netology.nmedia.util.AndroidUtils

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val initialText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        val initialVideo = intent.getStringExtra("video") ?: ""

        binding.edit.setText(initialText)
        binding.videoInput.setText(initialVideo)
        if (initialText.isNotEmpty()) {
            binding.edit.setSelection(initialText.length)
        }

        AndroidUtils.showKeyboard(binding.edit)

        binding.ok.setOnClickListener {
            val content = binding.edit.text.toString().trim()

            if (content.isBlank()) {
                binding.edit.error = getString(R.string.error_empty_content)
                return@setOnClickListener
            }

            val videoUrl = binding.videoInput.text.toString().trim()

//            if (videoUrl.isBlank()) {
//                binding.videoInput.error = getString(R.string.error_invalid_video_url)
//                return@setOnClickListener
//            }

            val intent = Intent().apply {
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra("video", videoUrl)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}