package sk.stuba.fei.mv.android.zaverecne.feed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListener
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.camera.CameraAcitivty
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedFragmentBinding


class FeedFragment : Fragment() {


    private val viewModel: FeedViewModel by lazy {
        ViewModelProvider(this).get(FeedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FeedFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

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
//                    openVideo()
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
}