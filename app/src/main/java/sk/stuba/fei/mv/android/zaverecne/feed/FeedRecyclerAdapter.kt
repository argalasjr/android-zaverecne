package sk.stuba.fei.mv.android.zaverecne.feed

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.feed_item.view.*
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
//        holder.itemView.setOnClickListener {
//            onClickListener.onClick(feedPost)
//        }
        holder.itemView.postMoreButton.setOnClickListener {
            onClickListener.onClick(feedPost)
        }

        holder.bind(feedPost)
    }

    class FeedPostViewHolder(private val binding: FeedItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(feedPost: FeedPost) {
            binding.post = feedPost
            binding.executePendingBindings()

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