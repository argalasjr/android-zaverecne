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
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlinx.android.synthetic.main.feed_fragment.*
import kotlinx.android.synthetic.main.feed_item.view.*
import sk.stuba.fei.mv.android.zaverecne.FeedPlayerAdapter.Companion.getVolume
import sk.stuba.fei.mv.android.zaverecne.FeedPlayerAdapter.Companion.onVolumeChange
import sk.stuba.fei.mv.android.zaverecne.feed.*
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository
import java.text.SimpleDateFormat
import java.util.*


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
            statusImageView.context.toast("An error occurred")
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
            .diskCacheStrategy(DiskCacheStrategy.NONE)
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
        // podmienka ci uzivatel ma nastavenu profilovu fotku
        if (profileSrc.isEmpty()) {
            // ak nema, tak sa nastavý default fotka profilu
            imgUri =
                Uri.parse("android.resource://" + view.context.packageName + "/drawable/profile_picture");
        } else {
            // ak má, tak priravy sa kompletná adresa pre fotku
            val fullPath = MasterRepository.mediaUrlBase + profileSrc
            imgUri = fullPath.toUri().buildUpon().scheme("http").build()
        }
        //https://android.jlelse.eu/best-strategy-to-load-images-using-glide-image-loading-library-for-android-e2b6ba9f75b2
        //SIGNATURE
        //https://stackoverflow.com/questions/47886247/how-to-reload-image-in-glide-from-the-same-url
        Glide.with(view.context)
            .load(imgUri)
            .apply(
                    RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            // signature - automaticky update fotky ktora je na rovnakej url adrese
                            .signature(ObjectKey(System.currentTimeMillis().toString()))
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

@BindingAdapter("date")
fun bindDate(textView: TextView, dateCreated: String?) {

    fun getDateDiff(oldDate: Date, nowDate: Date): Long {
        val diffInMillies = nowDate.time - oldDate.time;
        return diffInMillies
    }

    textView.let {
        val date1: Date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateCreated)
        val yearInMill = 31556926000
        val millisecond: Long = getDateDiff(date1, Date())
        val seconds = millisecond/1000
        val minutes = seconds/60
        val hours = minutes/60

        val time: String?
        Log.d("date", "$millisecond a $seconds")
        if(seconds < 60){
            time = "$seconds s"
        }
        else if(seconds >= 60 && minutes<60){
            time = "$minutes min"
        }
        else if(minutes>=60 && hours <= 24){
            time = "$hours h"
        }
        else if(hours > 24 && yearInMill > millisecond){
            time = SimpleDateFormat("dd.MM.").format(date1)
        }
        else{
            time = SimpleDateFormat("dd.MM.YYYY").format(date1)
        }

        textView.text = time
    }
}

@BindingAdapter("onClick")
fun setOnClick(imageView: ImageView, volume: Boolean) {
    if(volume){
        imageView.setBackgroundResource(R.drawable.ic_baseline_volume_up_24)
    }else{
        imageView.setBackgroundResource(R.drawable.ic_baseline_volume_off_24)
    }

    imageView.setOnClickListener {
        val curr_volume = getVolume() != 0f
        if(curr_volume){
            it.setBackgroundResource(R.drawable.ic_baseline_volume_off_24)
            onVolumeChange(!curr_volume)

        }else{
            it.setBackgroundResource(R.drawable.ic_baseline_volume_up_24)
            onVolumeChange(!curr_volume)
        }
    }
}


@BindingAdapter("feedPost", "viewModel")
fun setOnClickMenu(imageView: ImageView, feedpost: FeedPost, feedViewModel: FeedViewModel) {

    val context = imageView.context

    fun shareVideo(url: String?) {
        //2
        val intent = Intent(Intent.ACTION_SEND).apply {
            //3
            type = "video/mp4"
            //4
            putExtra(Intent.EXTRA_TEXT, url)
            //5
//            val uri = Uri.parse(url)
//            putExtra(Intent.EXTRA_STREAM, uri)
//            //6
//            val videoUrl = URL(url)
//            clipData = ClipData.newUri(context.contentResolver, context.getString(R.string.app_name), FileProvider.getUriForFile(context, FILE_PROVIDER, videoUrl))
            //7
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        //8
        context?.startActivity(Intent.createChooser(intent, null))
    }

     fun deleteVideoDialog(feedViewModel: FeedViewModel, feedPost: FeedPost) {
        val alert: AlertDialog.Builder = AlertDialog.Builder(context!!)
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(50, 0, 100, 0)
        alert.setMessage("Tento príspevok bude odstránený a už ho nebudete môcť nájsť..")
        alert.setView(linearLayout)

        alert.setNegativeButton(
                "Zrušiť",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        alert.setPositiveButton("Odstrániť", DialogInterface.OnClickListener { dialog, which ->
            feedViewModel.deleteUserPost(imageView, feedPost)
            Toast.makeText(context, "The post has been deleted.", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            feedViewModel.getUserPosts()
        })
        alert.show()

    }


    fun showMenu(feedPost: FeedPost) {
        val dialog = RoundedBottomSheetDialog(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val btnsheet = inflater.inflate(R.layout.bottom_sheet_dialog_fragment, null)
        dialog.setContentView(btnsheet)
        btnsheet.setOnClickListener {
        }
        dialog.show()

        val deletePostBtn =
                btnsheet.findViewById<LinearLayout>(R.id.deletePostButton)
        deletePostBtn.setOnClickListener(View.OnClickListener {
            deleteVideoDialog(feedViewModel, feedPost)
            dialog.dismiss()
        })

        val sharePostBtn =
                btnsheet.findViewById<LinearLayout>(R.id.shareButtonPost)
        sharePostBtn.setOnClickListener(View.OnClickListener {
            val url = MasterRepository.mediaUrlBase + feedPost.videoSrc
            shareVideo(url)
            dialog.dismiss()
        })
    }


    imageView.setOnClickListener(View.OnClickListener {
        showMenu(feedpost)
    })

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

                this.setOnClickListener(View.OnClickListener {
                })

                item_id?.let { id ->
                    if(exoPlayers.containsKey(id))
                        exoPlayers.remove(id)
                    exoPlayers[id] = player
                }
                player.addListener(
                        FeedRecyclerAdapter.FeedPlayerListener(
                                player,
                                context,
                                callback
                        )
                )
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
        fun onVolumeChange(volume: Boolean){
//            val currentVolume: Float
            if(!volume){
                exoPlayers.forEach {
                    it.value.volume = 0f
                }
            }else{
                exoPlayers.forEach {
                    it.value.volume = AudioManager.STREAM_MUSIC.toFloat()
                }
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

        @JvmStatic
        fun getVolume(): Float? {
            return exoPlayers[0]?.volume
        }

    }
}
