package sk.stuba.fei.mv.android.zaverecne.feed

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedItemBinding
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository
import java.io.File

class FeedRecyclerAdapter(private val onClickListener: OnClickListener) : ListAdapter<FeedPost,
        FeedRecyclerAdapter.FeedPostViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPostViewHolder {
        return FeedPostViewHolder(FeedItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: FeedPostViewHolder, position: Int) {
        val feedPost = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(feedPost)
        }
        holder.bind(feedPost)
    }

    class FeedPostViewHolder(private val binding: FeedItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(feedPost: FeedPost) {
            binding.post = feedPost
            binding.executePendingBindings()

            binding.postMoreButton.setOnClickListener(View.OnClickListener {
                showMenu(it,feedPost)
            })

            Log.d("Bind", "holder" + feedPost.username)
        }

        fun showMenu(view: View, feedPost: FeedPost) {
            val popup = PopupMenu(view.context, view)
           popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
            R.id.delete -> {

                GlobalScope.launch { // launch a new coroutine in background and continue
//                _status.value = ApiStatus.LOADING
                    try {
                        val activeUser = MasterRepository.dbExistsActiveUser()
                        activeUser?.let {
                            MasterRepository.removePost(activeUser.token,feedPost.postId)

                            val snackbar = Snackbar
                                .make(view, "The post has been deleted.", Snackbar.LENGTH_LONG)
                            snackbar.show()
                        }
                    } catch (e: Exception) {
//                    _status.value = ApiStatus.ERROR
//                    _posts.value = ArrayList()
                    }
                }



                true
            } else -> false
          }
    }
            popup.inflate(R.menu.post_menu)
            popup.show()
        }

    }

    class OnClickListener(val clickListener: (feedPost: FeedPost) -> Unit) {
        fun onClick(feedPost: FeedPost) = clickListener(feedPost)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FeedPost>() {
        override fun areItemsTheSame(oldItem: FeedPost, newItem: FeedPost): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: FeedPost, newItem: FeedPost): Boolean {
            return oldItem.postId == newItem.postId
        }
    }


}