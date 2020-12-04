package sk.stuba.fei.mv.android.zaverecne.feed

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
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

        val application = requireNotNull(this.activity).application
        val viewModelFactory = FeedViewModelFactory(application)
        val feedViewModel = ViewModelProvider(this, viewModelFactory).get(FeedViewModel::class.java)

        binding.lifecycleOwner = this

        binding.viewModel = feedViewModel

        binding.feed.adapter = FeedRecyclerAdapter(FeedRecyclerAdapter.OnClickListener{
            showMenu(feedViewModel, it)
        })

        //automaticky prehra aktualne video na obrazovke pri scrollovani
        binding.feed.addOnScrollListener(FeedRecyclerAdapter.FeedScrollListener())

        val anim = AnimationUtils.loadAnimation(context, R.anim.right_to_left)
        val layoutAnimationController = LayoutAnimationController(anim)

        binding.feed.layoutAnimation = layoutAnimationController


        binding.profilePic.setOnClickListener(View.OnClickListener {
//            Log.d("Profile", "profile")
            val navController = findNavController();
            navController.navigate(R.id.action_feedFragment_to_profileFragment)
        })


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


    fun showMenu(feedViewModel: FeedViewModel, feedPost: FeedPost) {
        val dialog = RoundedBottomSheetDialog(context!!)
        val btnsheet = layoutInflater.inflate(R.layout.bottom_sheet_dialog_fragment, null)
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

    private fun deleteVideoDialog(feedViewModel: FeedViewModel, feedPost: FeedPost) {
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
            feedViewModel.deleteUserPost(feed, feedPost)
            Toast.makeText(context, "The post has been deleted.", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            feedViewModel.getUserPosts()
        })
        alert.show()

    }

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
        releaseAllPlayers()
        Log.d("state", "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d("state", "onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d("state", "onStop")
        releasePlayer()
    }

    private fun releasePlayer() {

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

