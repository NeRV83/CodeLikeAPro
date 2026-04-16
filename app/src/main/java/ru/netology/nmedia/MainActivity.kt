package ru.netology.nmedia

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            likes = 9999,
            isLiked = false,
            shares = 5,
            views = 100

        )
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            if (post.isLiked) {
                like?.setImageResource(R.drawable.ic_liked_24)
            }
            likeCount?.text = formatShortNumber(post.likes)
            shareCount?.text = formatShortNumber(post.shares)
            viewCount?.text = formatShortNumber(post.views)

            root.setOnClickListener {
                Log.d("stuff", "stuff")
            }

            avatar.setOnClickListener {
                Log.d("stuff", "avatar")
            }

            like?.setOnClickListener {
                Log.d("stuff", "like")
                post.isLiked = !post.isLiked
                like.setImageResource(
                    if (post.isLiked) R.drawable.ic_liked_24 else R.drawable.ic_likes_24
                )
                if (post.isLiked) post.likes++ else post.likes--
                likeCount?.text = formatShortNumber(post.likes)
            }

            share?.setOnClickListener {
                Log.d("stuff","share")
                post.shares++
                shareCount?.text = formatShortNumber(post.shares)
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