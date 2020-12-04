package sk.stuba.fei.mv.android.zaverecne.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.android.synthetic.main.feed_fragment.*
import kotlinx.android.synthetic.main.profile_fragment.*
import sk.stuba.fei.mv.android.zaverecne.BuildConfig
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedFragmentBinding
import sk.stuba.fei.mv.android.zaverecne.databinding.ProfileFragmentBinding
import sk.stuba.fei.mv.android.zaverecne.fetchfiles.FetchFiles.getRealPathFromURI
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }
    private var _binding: ProfileFragmentBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    val REQUEST_CODE = 100
    lateinit var currentPhotoPath: String
    val REQUEST_CODE_CAM = 101

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = ProfileFragmentBinding.inflate(inflater)


        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        val viewModelFactory = ProfileViewModelFactory(application)


        // Get a reference to the ViewModel associated with this fragment.
        val profileViewModel =
            ViewModelProvider(
                    this, viewModelFactory
            ).get(ProfileViewModel::class.java)



        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)

        binding.profileViewModel = profileViewModel

        binding.profileViewModel = viewModel


        binding.apply {
            backProfil.setOnClickListener(View.OnClickListener {
                val navController = findNavController();
                navController.navigate(R.id.action_profileFragment_to_feedFragment)
            })

            profileImage.setOnClickListener(View.OnClickListener {
                val btnsheet = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
                val dialog = RoundedBottomSheetDialog(context!!)
                dialog.setContentView(btnsheet)
                btnsheet.setOnClickListener {
                }
                dialog.show()

                val chooseGalleryButton =
                        btnsheet.findViewById<LinearLayout>(R.id.chooseGalleryProfile)
                chooseGalleryButton.setOnClickListener(View.OnClickListener {
                    if (checkPermissionStorage()) {
                        openGalleryForImage()
                    } else {
                        requestPermission()
                    }
                    dialog.dismiss()
                })
                val chooseTakePhotoButton =
                        btnsheet.findViewById<LinearLayout>(R.id.takePhotoProfile)
                chooseTakePhotoButton.setOnClickListener(View.OnClickListener {

                    if (checkPermissionCamera()) {
                        dispatchTakePictureIntent()
                    } else {
                        requestPermissionCamera()
                    }
                    dialog.dismiss()

                })

                val deleteProfilePic = btnsheet.findViewById<LinearLayout>(R.id.deletePhoto)
                deleteProfilePic.setOnClickListener(View.OnClickListener {
                    profileViewModel.deleteProfilePhoto(it)
                    dialog.dismiss()
                })
            })

            linkChangePassButton.setOnClickListener(View.OnClickListener {
                val navController = findNavController();
                navController.navigate(R.id.action_profileFragment_to_resetFragment)
            })


            linkLogoutButton.setOnClickListener(View.OnClickListener {
                profileViewModel.logout()
                val navController = findNavController();
                navController.navigate(R.id.action_profileFragment_to_loginFragment)
            })
        }

        return binding.root


    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("leak", "onDestroy called")
        _binding = null
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {

                    val photoURI: Uri = FileProvider.getUriForFile(
                            context!!,
                            BuildConfig.APPLICATION_ID + ".fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    startActivityForResult(takePictureIntent, REQUEST_CODE_CAM)

                }
            }
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
//            bindprofileImage.setImageURI(data?.data) // handle chosen image
            val path = data?.data
            val realPath = getRealPathFromURI(context!!, path!!)
            val file = File(realPath)
            Log.d("filePath", file.absolutePath);
            viewModel.uploadProfilePhoto(container, file)

        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CAM && data != null){
//            imageView.setImageBitmap(data.extras?.get("data") as Bitmap)
            val file = bitmapToFile(data.extras?.get("data") as Bitmap, currentPhotoPath)
            viewModel.uploadProfilePhoto(container, file)
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
            PERMISSION_REQUEST_CODE_CAM -> cameraPermissionGranted(grantResults)
        }
    }

    fun cameraPermissionGranted(grantResults: IntArray){
        var valid = true
        for (grantResult in grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED
        }
        if (valid && !camera.isOpened) {
            dispatchTakePictureIntent()
        }
    }

    private fun checkPermissionCamera(): Boolean {
        val result =
                this.let { ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) }
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionStorage(): Boolean {
        val result = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpeg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
        )
    }

    private fun requestPermissionCamera() {
        ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE_CAM
        )
    }


    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return return try {
            file = File(fileNameToSave)

            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val PERMISSION_REQUEST_CODE_CAM = 101
    }

}