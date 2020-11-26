package sk.stuba.fei.mv.android.zaverecne.profile

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import sk.stuba.fei.mv.android.zaverecne.databinding.ProfileFragmentBinding
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository


class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }


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


        }

        return binding.root
    }



}