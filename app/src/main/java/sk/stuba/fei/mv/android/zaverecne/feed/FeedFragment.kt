package sk.stuba.fei.mv.android.zaverecne.feed

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListener
import kotlinx.android.synthetic.main.feed_fragment.*
import kotlinx.android.synthetic.main.feed_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.camera.CameraAcitivty
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedFragmentBinding
import sk.stuba.fei.mv.android.zaverecne.gallery.FolderRecycleView
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository


class FeedFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding =  FeedFragmentBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = FeedViewModelFactory(application)
        val feedViewModel = ViewModelProvider(this, viewModelFactory).get(FeedViewModel::class.java)

        binding.lifecycleOwner = this

        binding.viewModel = feedViewModel

        binding.feed.adapter = FeedRecyclerAdapter(FeedRecyclerAdapter.OnClickListener {
                showMenu(it)
//            Log.d("feed", it.postId+"\n" + it.username +"\n " + it.created+" \n" + it.profile+"\n " + it.title+"\n " + it.videoSrc);
        })


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


    fun showMenu(feedPost: FeedPost) {
        val dialog = RoundedBottomSheetDialog(context!!)
        val btnsheet = layoutInflater.inflate(R.layout.bottom_sheet_dialog_post, null)
        dialog.setContentView(btnsheet)
        btnsheet.setOnClickListener {
        }
        dialog.show()

        val deletePostBtn =
            btnsheet.findViewById<LinearLayout>(R.id.deletePostButton)
        deletePostBtn.setOnClickListener(View.OnClickListener {
            GlobalScope.launch { // launch a new coroutine in background and continue
//                _status.value = ApiStatus.LOADING
                try {
                    val activeUser = MasterRepository.dbExistsActiveUser()
                    activeUser?.let {
                        MasterRepository.removePost(activeUser.token, feedPost.postId)
                        val snackbar = Snackbar
                            .make(
                                feed,
                                "The post has been deleted.",
                                Snackbar.LENGTH_LONG
                            )
                        snackbar.show()
                    }
                } catch (e: Exception) {
//                    _status.value = ApiStatus.ERROR
//                    _posts.value = ArrayList()
                }
            }
            dialog.dismiss()
        })
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

