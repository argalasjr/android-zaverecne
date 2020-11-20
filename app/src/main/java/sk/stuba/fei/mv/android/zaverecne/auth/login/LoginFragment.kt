package sk.stuba.fei.mv.android.zaverecne.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.LoginFragmentBinding


class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = LoginFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.loginViewModel = viewModel

        binding.apply {
            btnLinkToRegister.setOnClickListener { view: View ->


                view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }

            login.setOnClickListener {view : View ->
                view.findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
            }
            }


        return binding.root
    }
}