package sk.stuba.fei.mv.android.zaverecne.auth.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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


        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        //val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        val viewModelFactory = LoginViewModelFactory(application)


        // Get a reference to the ViewModel associated with this fragment.
        val loginViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(LoginViewModel::class.java)



        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)


        binding.loginViewModel = viewModel

        binding.apply {

            loginViewModel.loginFormState.observe(viewLifecycleOwner,
                Observer { loginFormState ->
                    if (loginFormState == null) {
                        return@Observer
                    }
                    login.isEnabled = loginFormState.isDataValid
                    loginFormState.usernameError?.let {
                        username.error = getString(it)
                    }
                    loginFormState.passwordError?.let {
                        password.error = getString(it)
                    }
                })

            loginViewModel.loginResult.observe(viewLifecycleOwner,
                Observer { loginResult ->
                    loginResult ?: return@Observer
                    loading.visibility = View.GONE
                    loginResult.error?.let {
                        showLoginFailed(it)
                    }
                    loginResult.success?.let {
                        updateUiWithUser(it)
                        Navigation.createNavigateOnClickListener(
                            R.id.action_loginFragment_to_feedFragment)
                    }
                })

            val afterTextChangedListener = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // ignore
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // ignore
                }

                override fun afterTextChanged(s: Editable) {
                    loginViewModel.loginDataChanged(
                        username.text.toString(),
                        password.text.toString()
                    )
                }
            }


            username.addTextChangedListener(afterTextChangedListener)
            password.addTextChangedListener(afterTextChangedListener)
            password.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(
                        username.text.toString(),
                        password.text.toString()
                    )
                }
                false
            }

            login.setOnClickListener {
                Log.i("Login Fragment","loging clicked")
                loading.visibility = View.VISIBLE
                loginViewModel.login(
                    username.text.toString(),
                    password.text.toString()
                )
            }
            btnLinkToRegister.setOnClickListener { view: View ->

                Log.i("Login Fragment","link to register clicked")
                view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }


        }




        return binding.root
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.app_name) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}