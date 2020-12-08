package sk.stuba.fei.mv.android.zaverecne.video

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.video_fragment.*
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.VideoFragmentBinding


class VideoFragment : Fragment() {

    private lateinit var videoViewModel: VideoViewModel
    private var path : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = VideoFragmentBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application

        path= arguments!!.getString("videoUri").toString()

        val viewModelFactory = VideoViewModelFactory(application)

        videoViewModel = ViewModelProvider(this, viewModelFactory).get(VideoViewModel::class.java)

        binding.exoplayerView.exo_close.setOnClickListener(View.OnClickListener {
            setRetake()

        })
        binding.exoplayerView.exo_save.setOnClickListener(View.OnClickListener {
            uploadVideo(it)
            binding.exoplayerView.exo_save.isEnabled = false
        })

        return binding.root

    }

    private fun setRetake() {
        val navController = findNavController();
        navController.navigate(R.id.action_videoFragment_to_feedFragment)
    }

    private fun uploadVideo(view: View){
        videoViewModel.uploadVideo(view, path)
        val navController = findNavController();
        val shouldForceUpdateFeed = true
        val action = VideoFragmentDirections.actionVideoFragmentToFeedFragment(shouldForceUpdateFeed)
        navController.navigate(action)
    }


    companion object {
        fun newInstance(path: String?) = VideoFragment()
        }

    override fun onStart() {
        super.onStart()
        startExoPlayer()
    }

    override fun onDestroyView() {
        videoLayout.removeAllViews()
        super.onDestroyView()
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
        DefaultTrackSelector(context!!)
    }

    private val simpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(context!!)
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
                getString(R.string.app_name)
    }

    private val dataSourceFactory by lazy {
        DefaultDataSourceFactory(context!!, Util.getUserAgent(context!!, applicationName))
    }


    private val videoMediaSource by lazy {
        ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(path))
    }

    }
