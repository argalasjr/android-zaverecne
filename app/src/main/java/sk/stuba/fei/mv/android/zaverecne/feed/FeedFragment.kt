package sk.stuba.fei.mv.android.zaverecne.feed

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListener
import kotlinx.android.synthetic.main.feed_fragment.*
import kotlinx.android.synthetic.main.feed_item.*
import kotlinx.android.synthetic.main.feed_item.view.*
import sk.stuba.fei.mv.android.zaverecne.FeedPlayerAdapter
import sk.stuba.fei.mv.android.zaverecne.FeedPlayerAdapter.Companion.onVolumeChange
import sk.stuba.fei.mv.android.zaverecne.FeedPlayerAdapter.Companion.releaseAllPlayers
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedFragmentBinding
import sk.stuba.fei.mv.android.zaverecne.fetchfiles.FetchFiles.getRealPathFromURI
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository


class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FeedFragmentBinding.inflate(inflater)

        val application = activity!!.application
        val viewModelFactory = FeedViewModelFactory(application)
        val feedViewModel = ViewModelProvider(activity!!, viewModelFactory).get(FeedViewModel::class.java)

        binding.lifecycleOwner = this

        binding.viewModel = feedViewModel

        val args: FeedFragmentArgs by navArgs()
        if(args.shouldForceUpdateFeed) {
            feedViewModel.getUserPosts(5000L)
        }

        binding.feed.adapter = FeedRecyclerAdapter(FeedRecyclerAdapter.OnClickListener{
        }, feedViewModel)


        //automaticky prehra aktualne video na obrazovke pri scrollovani
        binding.feed.addOnScrollListener(FeedRecyclerAdapter.FeedScrollListener())

        val anim = AnimationUtils.loadAnimation(context, R.anim.right_to_left)
        val layoutAnimationController = LayoutAnimationController(anim)

        binding.feed.layoutAnimation = layoutAnimationController


        binding.profilePic.setOnClickListener(View.OnClickListener {
            val navController = findNavController();
            navController.navigate(R.id.action_feedFragment_to_profileFragment)
        })

        //** Set the colors of the Pull To Refresh View
        binding.swiperefresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context!!, R.color.material_green_900))
        binding.swiperefresh.setColorSchemeColors(Color.WHITE)

        binding.swiperefresh.setOnRefreshListener {
                releaseAllPlayers()
                feedViewModel.getUserPosts()
                binding.swiperefresh.isRefreshing = false
        }

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

        return binding.root
    }


    private fun captureVideo() {
        // Permission has already been granted
        val navController = findNavController();
        navController.navigate(R.id.action_feedFragment_to_cameraFragment)
    }

    private fun openGallery() {
        if (checkPermissionStorage()) {
            openGalleryForImage()
        } else {
            requestPermission()
        }
    }

    private fun checkPermissionStorage(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onPause() {
        super.onPause()

        Log.d("state", "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d("state", "onResume")
    }

    override fun onStop() {
        super.onStop()
        releaseAllPlayers()
        Log.d("state", "onStop")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            val path = data?.data
            val realPath = getRealPathFromURI(context!!, path!!)
            Log.d("realPath", realPath.toString())
            val args = Bundle()
            args.putString("videoUri", realPath);
            val navController = findNavController();
            navController.navigate(R.id.action_feedFragment_to_videoFragment, args)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .")
                openGalleryForImage()
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .")
            }
        }
    }


    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val REQUEST_CODE = 100
    }
}

