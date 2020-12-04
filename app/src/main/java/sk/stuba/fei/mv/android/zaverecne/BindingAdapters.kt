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
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlinx.android.synthetic.main.feed_item.view.*
import sk.stuba.fei.mv.android.zaverecne.feed.FeedPost
import sk.stuba.fei.mv.android.zaverecne.feed.FeedRecyclerAdapter
import sk.stuba.fei.mv.android.zaverecne.feed.PlayerStateCallback
import sk.stuba.fei.mv.android.zaverecne.feed.TopSpacingItemDecoration
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


@BindingAdapter("apiStatusFeed")
fun bindStatusFeed(statusImageView: View, status: ApiStatus?) {
    when (status) {
        ApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
        }
        ApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.context.toast("An error occurred while loading posts.")
        }
        ApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}


@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<FeedPost>?) {
    val adapter = recyclerView.adapter as FeedRecyclerAdapter
    val topSpacingDecorator = TopSpacingItemDecoration(30)
    recyclerView.addItemDecoration(topSpacingDecorator)
    recyclerView.scheduleLayoutAnimation()
    adapter.submitList(data)
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

@BindingAdapter("profile")
fun bindProfile(view: ImageView, profileSrc: String?) {
    profileSrc?.let {
        val imgUri: Uri
        //Kontrola ci uzivatel ma nastavenu profilovu fotku
        if (profileSrc.isEmpty()) {
            imgUri =
                Uri.parse("android.resource://" + view.context.packageName + "/drawable/profile_picture");
        } else {
            val fullPath = MasterRepository.mediaUrlBase + profileSrc
            imgUri = fullPath.toUri().buildUpon().scheme("http").build()
        }
        Glide.with(view.context)
            .load(imgUri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.profile_picture)
                    .error(R.drawable.ic_broken_image)
            )
            .into(view)

        Log.d("profilePic", MasterRepository.mediaUrlBase + it)
    }

}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            )
            .into(imgView)
    }
}

/*
    Pouzite zdroje k implementacii RecyclerView + ExoPlayer:

    https://medium.com/mindorks/working-with-exoplayer-the-clean-way-and-customization-fac81e5d39ba
    https://medium.com/@stlin813/how-to-implementing-video-playback-in-recyclerview-viewpager-part-1-4fcdccec4a4c
    https://medium.com/@stlin813/how-to-implementing-video-playback-in-recyclerview-viewpager-part-2-8df1b8d0d8fa
*/
class FeedPlayerAdapter {
    companion object {
        private var nowPlaying: Pair<Int, SimpleExoPlayer>? = null
        private var exoPlayers: MutableMap<Int, SimpleExoPlayer> = mutableMapOf()

        @JvmStatic
        fun autoPlayVisible(id: Int) {
            exoPlayers[id]?.let { player ->
                if (!player.playWhenReady) {
                    pauseNowPlaying()
                    player.playWhenReady = true
                    nowPlaying = Pair(id, player)
                }
            }
        }

        @JvmStatic
        fun pauseNowPlaying() {
            nowPlaying?.let {
                it.second.playWhenReady = false
            }
        }

        @JvmStatic
        @BindingAdapter("video", "on_state_change", "item_id")
        fun PlayerView.loadVideo(
            videoSrc: String,
            callback: PlayerStateCallback,
            item_id: Int? = null
        ) {
            val repo = MasterRepository(context)
            videoSrc.let {
                val trackSelection = DefaultTrackSelector(context);
                val player = SimpleExoPlayer.Builder(context)
                    .setTrackSelector(trackSelection)
                    .build()
                player.playWhenReady = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                setKeepContentOnPlayerReset(true)
                this.controllerHideOnTouch = true
                this.controllerShowTimeoutMs = 1000

                val fullUrl = repo.mediaUrlBase + videoSrc
                val mediaItem: MediaItem = MediaItem.fromUri(Uri.parse(fullUrl))
                val mediaSource =
                    ProgressiveMediaSource.Factory(DefaultHttpDataSourceFactory("demo"))
                        .createMediaSource(mediaItem)
                player.setMediaSource(mediaSource)
                player.prepare()

                item_id?.let { id ->
                    if(exoPlayers.containsKey(id))
                        exoPlayers.remove(id)
                    exoPlayers[id] = player
                }
                player.addListener(FeedRecyclerAdapter.FeedPlayerListener(player, context, callback))
                this.player = player
            }
        }

        @JvmStatic
        fun onViewRecycled(id: Int) {
            exoPlayers[id]?.let {
                it.release()
            }
        }

        @JvmStatic
        fun releaseAllPlayers() {
            //for proper garbage collection
            exoPlayers.forEach {
                it.value.release()
            }
            exoPlayers.clear()
        }
    }
}
