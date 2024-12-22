package com.example.nework2.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.nework2.R
import com.example.nework2.databinding.CardAdBinding
import com.example.nework2.databinding.FragmentPostCardBinding
import com.example.nework2.dto.Ad
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Post
import com.example.nework2.service.WallService
import com.example.nework2.view.load
import com.example.nework2.view.loadWithoutCircle


interface OnInteractionListener {
    fun onLike(post: Post)
    fun onRepost(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun playVideoInUri(post: Post)
    fun openPost(post: Post)
    fun openImage(post: Post)
}

typealias OnListener = (post: Post) -> Unit //можем вводить новые константы для типов, которые хотим использовать
typealias OnRemoveListener = (post: Post) -> Unit

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffUtil) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.fragment_post_card
            null -> error("unknow item type")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.fragment_post_card -> {
                val binding = FragmentPostCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PostViewHolder(binding, onInteractionListener)
            }

            R.layout.card_ad -> {
                val binding = CardAdBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AdViewHolder(binding)
            }

            else -> error("Unknow view type $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("Unknow item type")
        }
    }
}

class PostViewHolder(
    private val binding: FragmentPostCardBinding,
    private val onLInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    private val service = WallService()

    val urlNeto = "http://10.0.2.2:9999/avatars/netology.jpg"
    val urlSber = "http://10.0.2.2:9999/avatars/sber.jpg"
    val urlTcs = "http://10.0.2.2:9999/avatars/tcs.jpg"
    val url404 = "http://10.0.2.2:9999/avatars/404.png"

    @SuppressLint("QueryPermissionsNeeded")
    fun bind(post: Post) =
        binding.apply {
            post.authorAvatar.let { ivAvatar.load("http://10.0.2.2:9999/avatars/$it") } //присваиваем новую аватарку
            post.photoPost.let {
                photoIv.loadWithoutCircle("http://10.0.2.2:9999/media/${post.attachment?.url}")
                if (post.attachment != null) {
                    binding.photoIv.visibility = View.VISIBLE
                }
                //Если post.attachment равен null, то картинку нужно явно скрывать так как RecyclerView переиспользует элементы
                else binding.photoIv.visibility = View.GONE
            }
            tvAuthor.text = post.author
            tvPublished.text = post.published
            tvContent.text = post.content
            //следим за состоянием параметра likedByMe
            ivLikes.isChecked = post.likedByMe
            //текст будет записываться в атрибут text MaterialButton
            ivLikes.text = service.amount(post.likes)
            ivRepost.text = service.amount(post.reposts)

            ivMenu.isVisible = post.ownedByMe


            ivLikes.setOnClickListener {
                onLInteractionListener.onLike(post)

            }
            ivRepost.setOnClickListener {
                onLInteractionListener.onRepost(post)

            }
            ivMenu.setOnClickListener {
                PopupMenu(
                    it.context,
                    it
                ).apply {//все, что внутри функции apply вызываются на объекте
                    //на котором apply была вызвана, т.е. PopupMenu
                    inflate(R.menu.menu_options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_edit -> {
                                onLInteractionListener.onEdit(post)
                                true
                            }

                            R.id.menu_remove -> {
                                onLInteractionListener.onRemove(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }


            if (post.video !== null) {
                //делаем видимой группу с элементами видео
                groupVideo.visibility = View.VISIBLE
                tvVideoPublished.text = post.video.toString()
//                videoView.setImageResource(R.drawable.image_leo)
            }

            //условия для скрытия groupVideo
            if (post.video == null) {
                groupVideo.visibility = View.GONE
            }

            // обработчик нажатия на видео
            videoView.setOnClickListener {
                onLInteractionListener.playVideoInUri(post)
            }

            buttonPlay.setOnClickListener {
                onLInteractionListener.playVideoInUri(post)
            }

            //клик на контент -> перходим в отдельный пост
            tvContent.setOnClickListener {
                onLInteractionListener.openPost(post)
            }

            photoIv.setOnClickListener {
                onLInteractionListener.openImage(post)
            }
        }
}

object PostDiffUtil : DiffUtil.ItemCallback<FeedItem>() {
    //метод проверяет содержимое постов на равенство
    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        //добавим проверку, чтобы у рекламы и поста не совпали id
        if (oldItem::class != newItem::class) {
            return false
        }

        return newItem.id == oldItem.id
    }

    //метод проверяет посты на равенство сравнивая id постов
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem.id == newItem.id

}

class AdViewHolder(
    private val binding: CardAdBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        binding.imageAd.loadWithoutCircle("http://10.0.2.2:9999/media/${ad.image}")
    }
}

