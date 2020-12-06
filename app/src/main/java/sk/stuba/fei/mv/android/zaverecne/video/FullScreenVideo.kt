package sk.stuba.fei.mv.android.zaverecne.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.FragmentFullScreenVideoBinding
import sk.stuba.fei.mv.android.zaverecne.databinding.FragmentProfileImageViewBinding


private const val VIDEO_SRC = "videoSrc"

class FullScreenVideo : Fragment() {
    private var videoSrc: String? = null

    private var _binding: FragmentFullScreenVideoBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoSrc = it.getString(VIDEO_SRC)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFullScreenVideoBinding.inflate(inflater,container,false)

        binding.lifecycleOwner = this
        binding.videoSrc = videoSrc

        binding.playerViewFullScreen.exo_close.setOnClickListener(View.OnClickListener {
            val navController = findNavController();
            navController.navigate(R.id.action_fullScreenVideo_to_feedFragment)
        })

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String) =
            FullScreenVideo().apply {
                arguments = Bundle().apply {
                    putString(VIDEO_SRC, param1)
                }
            }
    }
}