package sk.stuba.fei.mv.android.zaverecne.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.ProfileFragmentBinding
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository
import java.io.File


class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    val REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ProfileFragmentBinding.inflate(inflater)


        val application = requireNotNull(this.activity).application


        // Create an instance of the ViewModel Factory.
        val viewModelFactory = ProfileViewModelFactory(application)


        // Get a reference to the ViewModel associated with this fragment.
        val profileViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(ProfileViewModel::class.java)



        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)


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

                val chooseGalleryButton = btnsheet.findViewById<LinearLayout>(R.id.chooseGalleryProfile)
                chooseGalleryButton.setOnClickListener(View.OnClickListener {
                    openGalleryForImage()
                })
                val chooseTakePhotoButton = btnsheet.findViewById<LinearLayout>(R.id.takePhotoProfile)
                chooseTakePhotoButton.setOnClickListener(View.OnClickListener {
                   openCamera()
                })
            })

        }
        return binding.root
    }

    private fun openCamera() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, REQUEST_CODE)
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
            uploadProfilePic(data?.data)
        }
    }

    fun uploadProfilePic(photoUri: Uri?){
        GlobalScope.launch { // launch a new coroutine in background and continue
//                _status.value = ApiStatus.LOADING
            try {
                val activeUser = MasterRepository.dbExistsActiveUser()
                activeUser?.let {
                    MasterRepository.uploadProfilePicture(activeUser.token, File(photoUri?.path))

                    Log.d("photo", File(photoUri?.path).path)

                    val snackbar = Snackbar
                        .make(container, "Succesfully uploaded.", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
            } catch (e: Exception) {
//                    _status.value = ApiStatus.ERROR
//                    _posts.value = ArrayList()
                val snackbar = Snackbar
                    .make(container, "Error during upload the photo.", Snackbar.LENGTH_LONG)
                snackbar.show()
            }
        }
    }

}