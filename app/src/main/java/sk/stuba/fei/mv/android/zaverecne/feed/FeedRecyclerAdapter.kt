package sk.stuba.fei.mv.android.zaverecne.feed

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.google.android.exoplayer2.Player
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.feed_item.view.*
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedItemBinding
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
//        Log.d("feedPost",  "Holder.oldPosition " +holder.oldPosition.toString() + "\n Post id" + feedPost.postId + "\n User name" + feedPost.username)
//        Log.d("feedPost",  "holder.layoutPosition. " +holder.layoutPosition.toString() + "\n isRecyclable " + holder.isRecyclable + "\n itemId " + holder.itemId )

    }

    class FeedPostViewHolder(private val binding: FeedItemBinding) : RecyclerView.ViewHolder(binding.root), PlayerStateCallback {
        fun bind(feedPost: FeedPost) {
            binding.post = feedPost
            binding.loadingPanel.startShimmerAnimation()
            binding.callback = this@FeedPostViewHolder
            binding.executePendingBindings()
        }

        override fun onVideoDurationRetrieved(duration: Long, player: Player) {
            binding.loadingPanel.visibility = View.GONE
            binding.progressBarBuffering.visibility = View.GONE
            binding.feedPostVideo.visibility = View.VISIBLE
            binding.loadingPanel.stopShimmerAnimation()
            Log.d("holder", "on video duration retrieved - callback")
        }

        override fun onVideoBuffering(player: Player) {
            Log.d("holder", "on video buffering - callback")
            binding.progressBarBuffering.visibility = View.VISIBLE
        }

        override fun onStartedPlaying(player: Player) {
            Log.d("holder", "on started playing - callback")
        }

        override fun onFinishedPlaying(player: Player) {
            Log.d("holder", "on finished playing - callback")
            player.seekTo(0)
            player.playWhenReady = false
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