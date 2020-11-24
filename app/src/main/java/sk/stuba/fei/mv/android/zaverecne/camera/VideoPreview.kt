package sk.stuba.fei.mv.android.zaverecne.camera

import android.app.Dialog
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import sk.stuba.fei.mv.android.zaverecne.MainActivity
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.feed.FeedFragment
import java.io.File
import java.util.*


class VideoPreview : AppCompatActivity() {

    private var isSaved = false
    private lateinit var exoPlayer: SimpleExoPlayer
    var TAG = "Video Preview"
    private var mFullScreenDialog: Dialog? = null
    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        //get data from intent
        videoUri= intent.data

        Log.d(TAG, "video uri: " + videoUri)

        exo_close.setOnClickListener(View.OnClickListener {
//            mFullScreenDialog.dismiss()
            setRetake()
        })
        exo_save.setOnClickListener(View.OnClickListener {
            val resIcon = getDrawable(R.drawable.ic_baseline_done_outline_24)

            exo_save.setEnabled(false)
            exo_save.setImageDrawable(resIcon)
            isSaved = true
            val intent = Intent (this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
            startActivity(intent)
        })
    }


    private fun setRetake(): Boolean {

            if (videoUri != null) {
                val file = File(videoUri.toString())
                if (file.exists()) {
                    finish()
                    return file.delete()
                }
        }
        return false
    }

    private fun initFullscreenDialog() {
        mFullScreenDialog =
            object : Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                override fun onBackPressed() {

                    if (isSaved) {

                    } else {
                        supportFinishAfterTransition()
                    }
                }
            }
    }


    private fun openFullscreenDialog() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        (exoplayerView.parent as ViewGroup).removeView(exoplayerView)
        this.mFullScreenDialog!!.addContentView(
                exoplayerView,
                ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        )
        //mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(VideoViewer.this, R.mipmap.ic_fullscreen_skrink_foreground));
        this.mFullScreenDialog!!.show()
    }

    override fun onStart() {
        super.onStart()
        startExoPlayer()
    }

    private fun stopExoPlayer() {
        simpleExoPlayer.stop()
        simpleExoPlayer.release()
    }

    override fun onStop() {
        stopExoPlayer()
        super.onStop()
    }

    private val bandwidthMeter by lazy {
        DefaultBandwidthMeter()
    }

    private val trackSelectionFactory by lazy {
        AdaptiveTrackSelection.Factory(bandwidthMeter)
    }

    private val trackSelection by lazy {
        DefaultTrackSelector(trackSelectionFactory)
    }

    private val simpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this, trackSelection)
    }

    private fun startExoPlayer() {
        exoplayerView.controllerAutoShow = false
        exoplayerView.player = simpleExoPlayer
        simpleExoPlayer.prepare(videoMediaSource)
        simpleExoPlayer.playWhenReady = true
    }

    private val applicationName by lazy {
        this.packageManager?.let { this.applicationInfo?.loadLabel(it).toString() }
    }

    private val dataSourceFactory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, applicationName))
    }

    private val videoMediaSource by lazy {
        ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(videoUri)
    }

}
