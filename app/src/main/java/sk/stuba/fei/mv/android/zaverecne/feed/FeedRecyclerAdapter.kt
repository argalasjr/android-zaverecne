package sk.stuba.fei.mv.android.zaverecne.feed

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.feed_item.view.*
import sk.stuba.fei.mv.android.zaverecne.FeedPlayerAdapter
import sk.stuba.fei.mv.android.zaverecne.FeedPlayerAdapter.Companion.loadVideo
import sk.stuba.fei.mv.android.zaverecne.FeedPlayerAdapter.Companion.onViewRecycled
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedItemBinding
import sk.stuba.fei.mv.android.zaverecne.toast

class FeedRecyclerAdapter(private val onClickListener: OnClickListener) : ListAdapter<FeedPost,
        FeedRecyclerAdapter.FeedPostViewHolder>(DiffCallback) {

    override fun onViewRecycled(holder: FeedPostViewHolder) {
        val position = holder.adapterPosition
        onViewRecycled(position)
        super.onViewRecycled(holder)
    }

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

    class FeedScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val id = layoutManager.findFirstCompletelyVisibleItemPosition()
            if (id != -1)
                FeedPlayerAdapter.autoPlayVisible(id)
        }
    }

    class FeedPostViewHolder(private val binding: FeedItemBinding) :
        RecyclerView.ViewHolder(binding.root), PlayerStateCallback {
        fun bind(feedPost: FeedPost) {
            binding.post = feedPost
            binding.loadingPanel.startShimmer()
            binding.callback = this@FeedPostViewHolder
            binding.itemId = adapterPosition
            binding.executePendingBindings()
            binding.volumeState = FeedPlayerAdapter.getVolume() != 0f
        }

        override fun onVideoDurationRetrieved(duration: Long, player: Player) {
            binding.loadingPanel.visibility = View.GONE
            binding.progressBarBuffering.visibility = View.GONE
            binding.feedPostVideo.visibility = View.VISIBLE
            binding.loadingPanel.stopShimmer()
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

    //listener for Player events on feed
    class FeedPlayerListener(
        val player: Player,
        val context: Context,
        val callback: PlayerStateCallback
    ) : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException) {
            super.onPlayerError(error)
            context.toast("An error occurred while playing media.")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, playbackState)

            if (playbackState == Player.STATE_BUFFERING) {
                callback.onVideoBuffering(player)
            }
            if (playbackState == Player.STATE_READY) {
                callback.onVideoDurationRetrieved((player as SimpleExoPlayer).duration, player)
            }
            if (playbackState == Player.STATE_READY && player.playWhenReady) {
                callback.onStartedPlaying(player)
            }
            if (playbackState == Player.STATE_ENDED) {
                callback.onFinishedPlaying(player)
            }

        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            super.onPlaybackParametersChanged(playbackParameters)
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
        }
    }

}