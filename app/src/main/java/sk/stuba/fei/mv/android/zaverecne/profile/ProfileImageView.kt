package sk.stuba.fei.mv.android.zaverecne.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.FragmentProfileImageViewBinding


class ProfileImageView : Fragment() {

    private var _binding: FragmentProfileImageViewBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileImageViewBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        val viewModelFactory = ProfileImageViewModelFactory(application)


        // Get a reference to the ViewModel associated with this fragment.
        val profileImageViewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(ProfileImageViewModel::class.java)


        binding.lifecycleOwner = this
        binding.profileImageViewModel = profileImageViewModel

        binding.closeProfileImageView.setOnClickListener(View.OnClickListener {
            val navController = findNavController();
            navController.navigate(R.id.action_profileImageView_to_profileFragment)
        })

        return binding.root
    }

    companion object {

    }

}