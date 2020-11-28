package sk.stuba.fei.mv.android.zaverecne.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.VideoCodec
import com.otaliastudios.cameraview.size.Size
import kotlinx.android.synthetic.main.camera_fragment.*
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.anim.Animations.animateViewFadeIn
import sk.stuba.fei.mv.android.zaverecne.anim.Animations.animateViewFadeOut
import sk.stuba.fei.mv.android.zaverecne.databinding.CameraFragmentBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraFragment : Fragment() {

    private var mVideoFolder: File? = null
    private var mVideoFileName: String? = null
    private var mIsRecording = false

    companion object {
        fun newInstance() = CameraFragment()
    }

    private lateinit var viewModel: CameraViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = CameraFragmentBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application

        val viewModelFactory = CameraViewModelFactory(application)

        val cameraViewModel = ViewModelProvider(this, viewModelFactory).get(CameraViewModel::class.java)

        binding.camera.setLifecycleOwner(this)
        binding.camera.mode = Mode.VIDEO
        binding.camera.facing = Facing.BACK
        binding.camera.videoCodec = VideoCodec.H_264
        binding.camera.flash = Flash.OFF

        createVideoFolder()
        binding.videoOnlineImageButton.setOnClickListener(View.OnClickListener {
            mIsRecording = true
            checkWriteStoragePermission()
        })

        binding.videoRecording.setOnClickListener(View.OnClickListener {
            stopRecording()
            animateViewFadeOut(videoRecording)
            animateViewFadeIn(videoOnlineImageButton)
        })


        binding.closeCam.setOnClickListener(View.OnClickListener() {
            val navController = findNavController();
            navController.navigate(R.id.action_cameraFragment_to_feedFragment)
        });

        binding.camera.addCameraListener(object : CameraListener() {
            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)
                Log.w("daAD", "onVideoTaken called! Launching activity.")
                navigateToVideo(result)
            }

            override fun onVideoRecordingStart() {
                // Notifies that the actual video recording has started.
                // Can be used to show some UI indicator for video recording or counting time.
            }

            override fun onVideoRecordingEnd() {
                // Notifies that the actual video recording has ended.
                // Can be used to remove UI indicators added in onVideoRecordingStart.
            }
        })

        binding.camera.setVideoSize { source ->
            val dm = resources.displayMetrics
            val densityDpi = dm.densityDpi
            val height = dm.heightPixels
            val width = dm.widthPixels
            val optimalSizes: MutableList<Size> =
                ArrayList()
            //                optimalSizes.add(getOptimalPreviewSize(source, width, height));
            Log.d("size", "velkost: $width, $height")
            Log.d(
                "size",
                "velkost: " + getOptimalPreviewSize(
                    source,
                    width,
                    height
                ).toString()
            )
            for (s in source) {
                Log.d("size", "velkost: $s")
                val separated = s.toString().split("x").toTypedArray()
                val w = separated[0].toDouble()
                val h = separated[1].toDouble()
                val ratio = h / w
                if (ratio >= 1.75 && ratio <= 1.85) {
                    optimalSizes.add(s)
                }
            }
            optimalSizes
        }

        //CHANGE CAMERA - FRONT/BACK
        binding.flipFacingCamera.setOnClickListener(View.OnClickListener {
            if (activity!!.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)!!) {
                if (binding.camera.facing == Facing.FRONT) {
                    binding.camera.facing = Facing.BACK
//                    if (hasFlash) {
//                        flashOn.setVisibility(View.VISIBLE)
//                        flashOff.setVisibility(View.GONE)
//                    } else {
//                        flashOn.setVisibility(View.GONE)
//                        flashOff.setVisibility(View.VISIBLE)
//                    }
                } else {
                    binding.camera.facing = Facing.FRONT
//                    flashOn.setVisibility(View.GONE)
//                    flashOff.setVisibility(View.GONE)
                }
                rotateFlipCameraIcon()
            } else {
                Toast.makeText(
                    context,
                    "Your device doesn't have a front camera..",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun checkPermission(): Boolean {
        val result =
            this.let { ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) }
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onPause() {
        if (checkPermission()) {
            camera.open()
        }
        super.onPause()
    }

    private fun rotateFlipCameraIcon() {
        flip_facing_camera.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.rotate)
        )
    }

    fun getOptimalPreviewSize(
        sizes: List<Size>?, w: Int, h: Int
    ): Size? {
        // Use a very small tolerance because we want an exact match.
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = w.toDouble() / h
        if (sizes == null) return null
        var optimalSize: Size? = null /*  w  w  w  .ja va  2 s .c  om*/

        // Start with max value and refine as we iterate over available preview sizes. This is the
        // minimum difference between view and camera height.
        var minDiff = Double.MAX_VALUE

        // Target view height

        // Try to find a preview size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }

    private fun stopRecording() {
        chronometer!!.handler.post { chronometer.visibility = View.INVISIBLE }
        mIsRecording = false
        camera.stopVideo() //ukoncenie zaznamenavania
        flip_facing_camera.visibility = View.VISIBLE
        close_cam.visibility = View.VISIBLE

    }

    @Throws(CameraAccessException::class)
    private fun getBackFacingCameraId(cManager: CameraManager): String? {
        for (cameraId in cManager.cameraIdList) {
            val characteristics = cManager.getCameraCharacteristics(cameraId)
            val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
            if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                Log.d("facing back", cameraId)
                return cameraId
            }
        }
        return null
    }

    private fun captureVideo() {
        if (!camera.isTakingVideo) {
            camera.takeVideo(File(mVideoFileName.toString()))
        } else {

        }
    }

    private fun createVideoFolder() {
        val aDirArray = this.let {
            ContextCompat.getExternalFilesDirs(
                context!!,
                Environment.DIRECTORY_DCIM
            )
        }

//        val aExtDcimDir = File(aDirArray[0],null)
        mVideoFolder = aDirArray[0]
        if (!mVideoFolder!!.exists()) {
            mVideoFolder!!.mkdirs()

        }
    }

    private fun navigateToVideo(result: VideoResult) {

        val args = Bundle()
        args.putString("videoUri", result.file.absolutePath);
        val navController = findNavController();
        navController.navigate(R.id.action_cameraFragment_to_videoFragment,args)
    }

    @Throws(IOException::class)
    private fun createVideoFileName(): File? {
        @SuppressLint("SimpleDateFormat") val timestamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(
                Date()
            )
        val prepend = "VIDEO_" + timestamp + "_"
        val videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder)
        mVideoFileName = videoFile.absolutePath
        return videoFile
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var valid = true
        for (grantResult in grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED
        }
        if (valid && !camera.isOpened) {
            camera.open()
        }
    }

    private fun checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.let { ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) }
                == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    createVideoFileName()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (mIsRecording) {
                    captureVideo()

                    close_cam.visibility = View.GONE
                    flip_facing_camera.visibility = View.GONE
                    animateViewFadeOut(videoOnlineImageButton)
                    animateViewFadeIn(videoRecording)
                    if (camera.facing != Facing.BACK) {
//                        flashOn.setVisibility(View.GONE)
//                        flashOff.setVisibility(View.GONE)
                    }
                    // chronometer spúšťa časovač pre zaznaménavanie
                    chronometer.base = SystemClock.elapsedRealtime()
                    chronometer.visibility = View.VISIBLE
                    chronometer.start()
                }
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(
                        context,
                        "app needs to be able to save videos",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                )
            }
        } else {
            try {
                createVideoFileName()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (mIsRecording) {
                captureVideo()
                close_cam.visibility = View.GONE
                flip_facing_camera.visibility = View.GONE
                //closeCam.setVisibility(View.GONE);
//                animateViewFadeOut(startRecordBtn) //GONE
//                animateViewFadeIn(recordingBtn) //VISIBLE
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                    bottomBar.setBackgroundColor(getColor(R.color.invisible))
//                }
                if (camera.facing != Facing.BACK) {
//                    flashOn.setVisibility(View.GONE)
//                    flashOff.setVisibility(View.GONE)
                }
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.visibility = View.VISIBLE
                chronometer.start()

            }
        }
    }


}