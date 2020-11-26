package sk.stuba.fei.mv.android.zaverecne.camera

import android.app.Dialog
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
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import sk.stuba.fei.mv.android.zaverecne.MainActivity
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository
import java.io.File
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.feed.ApiStatus
import sk.stuba.fei.mv.android.zaverecne.feed.FeedPost


class VideoPreview : AppCompatActivity() {

    private var isSaved = false
    var TAG = "Video Preview"
    private var mFullScreenDialog: Dialog? = null
    private lateinit var videoUri: Uri

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
        videoUri= intent.data!!

        exo_close.setOnClickListener(View.OnClickListener {
            setRetake()
        })
        exo_save.setOnClickListener(View.OnClickListener {
            val resIcon = getDrawable(R.drawable.ic_baseline_done_outline_24)
            exo_save.isEnabled = false
            exo_save.setImageDrawable(resIcon)
            isSaved = true
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
            startActivity(intent)

            GlobalScope.launch { // launch a new coroutine in background and continue
//                _status.value = ApiStatus.LOADING
                try {
                    val activeUser = MasterRepository.dbExistsActiveUser()
                    activeUser?.let {
                       Log.d("user", "token - " +activeUser.token)
                        Log.d("user", "name - " + activeUser.userName)
                        MasterRepository.uploadPost(activeUser.token,File(videoUri.path))
                    }

                } catch (e: Exception) {
//                    _status.value = ApiStatus.ERROR
//                    _posts.value = ArrayList()
                }


            }

        })
    }

    private fun setRetake(): Boolean {

                val file = File(videoUri.path)
                if (file.exists()) {
                    onBackPressed()
                    return true
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


    private val trackSelection by lazy {
        DefaultTrackSelector(this);
    }

    private val simpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelection)
            .build()
    }

    private fun startExoPlayer() {
        exoplayerView.controllerAutoShow = false
        exoplayerView.player = simpleExoPlayer
        simpleExoPlayer.setMediaSource(videoMediaSource)
        simpleExoPlayer.prepare()
        simpleExoPlayer.playWhenReady = true
    }

    private val applicationName by lazy {
        this.packageManager.let { this.applicationInfo.loadLabel(it).toString() }
    }

    private val dataSourceFactory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, applicationName))
    }

    private val videoMediaSource by lazy {
        ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoUri))
    }

}
