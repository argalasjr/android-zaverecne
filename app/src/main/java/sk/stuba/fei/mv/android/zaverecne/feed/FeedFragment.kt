package sk.stuba.fei.mv.android.zaverecne.feed

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListener
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.camera.CameraAcitivty
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedFragmentBinding

import sk.stuba.fei.mv.android.zaverecne.gallery.FolderRecycleView


class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FeedFragmentBinding.inflate(inflater)
        val application = requireNotNull(this.activity).application
        val viewModelFactory = FeedViewModelFactory(application)
        val feedViewModel = ViewModelProvider(this, viewModelFactory).get(FeedViewModel::class.java)

        binding.lifecycleOwner = this

        binding.viewModel = feedViewModel

        binding.feed.adapter = FeedRecyclerAdapter(FeedRecyclerAdapter.OnClickListener {

        })

//
        binding.speedDial.addActionItem(
            SpeedDialActionItem.Builder(
                R.id.recordVideo,
                R.drawable.ic_baseline_videocam_24
            ).setLabel(getString(R.string.record_video))
                .setTheme(R.style.AppTheme_Purple)
                .setLabelClickable(true)
                .create()
        )

        binding.speedDial.addActionItem(
            SpeedDialActionItem.Builder(
                R.id.openVideo,
                R.drawable.ic_baseline_video_library_24
            ).setLabel(getString(R.string.select_from_gallery))
                .setTheme(R.style.AppTheme_Purple)
                .setLabelClickable(true)
                .create()
        )


        binding.speedDial.setOnActionSelectedListener(OnActionSelectedListener { speedDialActionItem ->
            when (speedDialActionItem.id) {
                R.id.recordVideo -> {
                    captureVideo()
                    false // true to keep the Speed Dial open
                }
                R.id.openVideo -> {
                    openGallery()
                    false // true to keep the Speed Dial open
                }
                else -> false
            }
        })
        binding.speedDialProfile.addActionItem(
            SpeedDialActionItem.Builder(
                R.id.openProfile,
                R.drawable.ic_baseline_video_library_24
            ).setLabel(getString(R.string.profile))
                .setTheme(R.style.AppTheme_Purple)
                .setLabelClickable(true)
                .create()
        )


        binding.speedDialProfile.setOnActionSelectedListener(OnActionSelectedListener { speedDialActionItem ->
            when (speedDialActionItem.id) {
                R.id.openProfile -> {
                    val navController = findNavController();
                    navController.navigate(R.id.action_feedFragment_to_profileFragment)
                    false // true to keep the Speed Dial open
                }
                else -> false
            }
        })



        return binding.root
    }

    private fun captureVideo() {
        // Permission has already been granted
        val intent = Intent(activity, CameraAcitivty::class.java)
        startActivity(intent)
    }

    private fun openGallery() {
        // Permission has already been granted
        val i = Intent(activity, FolderRecycleView::class.java)
        val options = ActivityOptions.makeSceneTransitionAnimation(activity)
        startActivity(i, options.toBundle())
    }
}