/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package sk.stuba.fei.mv.android.zaverecne

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import sk.stuba.fei.mv.android.zaverecne.feed.ApiStatus
import sk.stuba.fei.mv.android.zaverecne.feed.FeedPost
import sk.stuba.fei.mv.android.zaverecne.feed.FeedRecyclerAdapter
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository


fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

@BindingAdapter("apiStatus")
fun bindStatus(statusImageView: ImageView, status: ApiStatus?) {
    when (status) {
        ApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
        ApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        ApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<FeedPost>?) {
    val adapter = recyclerView.adapter as FeedRecyclerAdapter
    adapter.submitList(data)
}


//https://medium.com/mindorks/working-with-exoplayer-the-clean-way-and-customization-fac81e5d39ba
@BindingAdapter(value = ["video", "thumbnail"], requireAll = false)
fun PlayerView.loadVideo(videoSrc: String, thumbnail: ImageView) {
    val repo = MasterRepository(context)
    videoSrc?.let {
        val player = SimpleExoPlayer.Builder(context).build()
        player.playWhenReady = false
        player.repeatMode = Player.REPEAT_MODE_ALL
        setKeepContentOnPlayerReset(true)
        this.useController = true

        val fullUrl = repo.mediaUrlBase + videoSrc
        val mediaItem: MediaItem = MediaItem.fromUri(Uri.parse(fullUrl))
        val mediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSourceFactory("Demo")).createMediaSource(mediaItem)
        player.setMediaSource(mediaSource)
        this.player = player

        this.player!!.addListener(object: Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                this@loadVideo.context.toast("An error occurred while playing media.")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, playbackState)

                if (playbackState == Player.STATE_BUFFERING) {
                    thumbnail.visibility = View.VISIBLE
                }
                if (playbackState == Player.STATE_READY) {
                    thumbnail.visibility = View.GONE
                }

            }
        })
    }
}

@BindingAdapter("thumbnail")
fun bindThumbnail(view: ImageView, thumbnailSrc: String?) {
    thumbnailSrc?.let {
        val imgUri = Uri.parse(thumbnailSrc)
        Glide.with(view.context)
            .load(imgUri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            )
            .into(view)
    }
}
