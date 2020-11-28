package sk.stuba.fei.mv.android.zaverecne.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.*
import com.otaliastudios.cameraview.size.Size
import kotlinx.android.synthetic.main.activity_camera_acitivty.*
import sk.stuba.fei.mv.android.zaverecne.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraAcitivty : AppCompatActivity(){

    private val TAG = CameraAcitivty::class.qualifiedName
    private var mVideoFolder: File? = null
    private var mVideoFileName: String? = null
    private var mIsRecording = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_acitivty)
        camera.setLifecycleOwner(this)
        camera.mode = Mode.VIDEO
//        camera.audio = Audio.ON
        camera.facing = Facing.BACK
        camera.videoCodec = VideoCodec.H_264

        camera.flash = Flash.OFF

        createVideoFolder()
        videoOnlineImageButton.setOnClickListener(View.OnClickListener {

            mIsRecording = true
            checkWriteStoragePermission()
        })

        videoRecording.setOnClickListener(View.OnClickListener {
            stopRecording()
            animateViewFadeOut(videoRecording)
            animateViewFadeIn(videoOnlineImageButton)
        })


        close_cam.setOnClickListener(View.OnClickListener() {
            finish()
        });

        camera.addCameraListener(object : CameraListener() {
            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)
                Log.w("daAD", "onVideoTaken called! Launching activity.")
                sendIntent(result)
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


        // This will be the size of videos taken with takeVideo().
        camera.setVideoSize { source ->
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
        flip_facing_camera.setOnClickListener(View.OnClickListener {
            if (this.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)!!) {
                if (camera.facing == Facing.FRONT) {
                    camera.facing = Facing.BACK
//                    if (hasFlash) {
//                        flashOn.setVisibility(View.VISIBLE)
//                        flashOff.setVisibility(View.GONE)
//                    } else {
//                        flashOn.setVisibility(View.GONE)
//                        flashOff.setVisibility(View.VISIBLE)
//                    }
                } else {
                    camera.facing = Facing.FRONT
//                    flashOn.setVisibility(View.GONE)
//                    flashOff.setVisibility(View.GONE)
                }
                rotateFlipCameraIcon()
            } else {
                Toast.makeText(
                        this,
                        "Your device doesn't have a front camera..",
                        Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun checkPermission(): Boolean {
        val result =
            this.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
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
                AnimationUtils.loadAnimation(this, R.anim.rotate)
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
                    it,
                    Environment.DIRECTORY_DCIM
            )
        }

//        val aExtDcimDir = File(aDirArray[0],null)
        mVideoFolder = aDirArray[0]
        if (!mVideoFolder!!.exists()) {
            mVideoFolder!!.mkdirs()

        }
    }

    private fun sendIntent(result: VideoResult) {
        val intent = Intent(this, VideoPreview::class.java)
        intent.data = Uri.fromFile(result.file)
        Log.d(TAG, "video uri: " + Uri.fromFile(result.file))
        startActivity(intent)
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
            // kontrola povolenia pre zápis do uložiska
            if (this.let { ContextCompat.checkSelfPermission(
                            it,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) }
                == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    // vytvaranie výstupného súboru pre zaznaménavanie
                    createVideoFileName()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (mIsRecording) {
                    // metóda spúšťa zaznamenávanie spolu s hudbou v pozadí
                    captureVideo()

                    // nastavovanie parametrov pre komponenty zabudované v kamere
                    // určite komponenty sú neviditeľne počas zaznamenávania
                    // GONE -  parametrom nastavíme neviditeľnosť komponenta
                    // VISIBLE -  parametrom nastavíme viditeľnosť komponenta
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
                            this,
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
                flip_facing_camera.setVisibility(View.GONE)
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

    private fun animateViewFadeOut(view: View) {
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.scale_down)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                view.visibility = View.GONE
            }

            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })
        view.startAnimation(fadeOut)
    }

    private fun animateViewFadeIn(view: View) {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })
        view.startAnimation(fadeIn)
    }
}