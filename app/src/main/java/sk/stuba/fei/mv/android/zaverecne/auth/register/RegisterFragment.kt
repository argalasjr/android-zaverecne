package sk.stuba.fei.mv.android.zaverecne.auth.register

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
import androidx.navigation.fragment.findNavController
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.auth.login.LoggedInUserView
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

        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        //val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        val viewModelFactory = RegisterViewModelFactory(application)
        // Get a reference to the ViewModel associated with this fragment.
        val registerViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(RegisterViewModel::class.java)

        binding.setLifecycleOwner(this)


        binding.registerViewModel = viewModel


        binding.apply {

            registerViewModel.registerFormState.observe(viewLifecycleOwner,
                Observer { registerFormState ->
                    if (registerFormState == null) {
                        return@Observer
                    }
                    register.isEnabled = registerFormState.isDataValid
                    registerFormState.usernameError?.let {
                        username.error = getString(it)
                    }
                    registerFormState.passwordError?.let {
                        password.error = getString(it)
                    }

                    registerFormState.emailError?.let {
                        password.error = getString(it)
                    }
                })

            registerViewModel.registerResult.observe(viewLifecycleOwner,
                Observer { registerResult ->
                    registerResult ?: return@Observer
                    loading.visibility = View.GONE
                    registerResult.error?.let {
                       showRegisterFailed(it)
                    }
                    registerResult.success?.let {
                        updateUiWithUser(it)
                        val navController = findNavController();
                        navController.navigate(R.id.action_registerFragment_to_feedFragment)
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
                    registerViewModel.registerDataChanged(
                        username.text.toString(),
                        password.text.toString(),
                        email.text.toString()
                    )
                }
            }


            username.addTextChangedListener(afterTextChangedListener)
            password.addTextChangedListener(afterTextChangedListener)
            email.addTextChangedListener(afterTextChangedListener)

            password.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    registerViewModel.register(
                        username.text.toString(),
                        password.text.toString(),
                        email.text.toString()
                    )
                }
                false
            }

            register.setOnClickListener {
                Log.i("Register Fragment","register clicked")
                loading.visibility = View.VISIBLE
                registerViewModel.register(
                    username.text.toString(),
                    password.text.toString(),
                    email.text.toString()
                )
            }
            
            
        }


            return binding.root
    }
    private fun updateUiWithUser(model: RegisteredUserView) {
        val welcome = getString(R.string.app_name) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showRegisterFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

}