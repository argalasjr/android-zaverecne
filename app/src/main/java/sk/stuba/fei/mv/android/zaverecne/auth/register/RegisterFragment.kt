package sk.stuba.fei.mv.android.zaverecne.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import sk.stuba.fei.mv.android.zaverecne.databinding.RegisterFragmentBinding

class RegisterFragment : Fragment(){

    private val viewModel: RegisterViewModel by lazy {
        ViewModelProvider(this).get(RegisterViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = RegisterFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.registerViewModel = viewModel

        return binding.root
    }
}