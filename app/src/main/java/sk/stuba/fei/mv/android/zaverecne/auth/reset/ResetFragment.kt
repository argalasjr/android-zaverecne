package sk.stuba.fei.mv.android.zaverecne.auth.reset




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
import sk.stuba.fei.mv.android.zaverecne.databinding.ResetFragmentBinding
import sk.stuba.fei.mv.android.zaverecne.util.Utils

class ResetFragment : Fragment(){

    private val viewModel: ResetViewModel by lazy {
        ViewModelProvider(this).get(ResetViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ResetFragmentBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        //val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        val viewModelFactory = ResetViewModelFactory(application)
        // Get a reference to the ViewModel associated with this fragment.
        val resetViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(ResetViewModel::class.java)

        binding.setLifecycleOwner(this)


        binding.resetViewModel = viewModel


        binding.apply {

            backProfil.setOnClickListener(View.OnClickListener {
                val navController = findNavController();
                navController.navigate(R.id.action_resetFragment_to_profileFragment)
            })

            resetViewModel.resetFormState.observe(viewLifecycleOwner,
                Observer { resetFormState ->
                    if (resetFormState == null) {
                        return@Observer
                    }
                    reset.isEnabled = resetFormState.isDataValid

                    resetFormState.oldPasswordError?.let {
                        oldPassword.error = getString(it)
                    }

                    resetFormState.newPasswordError?.let {
                        newPassword.error = getString(it)
                    }
                    resetFormState.verifyNewPasswordError?.let {
                        verifyNewPassword.error = getString(it)
                    }


                })

            resetViewModel.resetResult.observe(viewLifecycleOwner,
                Observer { resetResult ->
                    resetResult ?: return@Observer
                    loading.visibility = View.GONE
                    resetResult.error?.let {
                        showResetFailed(it)
                    }
                    resetResult.success?.let {
                        updateUiWithUser(it)
                        val navController = findNavController();
                         navController.navigate(R.id.action_resetFragment_to_profileFragment)
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
                    resetViewModel.resetDataChanged(
                        oldPassword.text.toString(),
                        newPassword.text.toString(),
                        verifyNewPassword.text.toString()
                    )
                }
            }




            oldPassword.addTextChangedListener(afterTextChangedListener)
            newPassword.addTextChangedListener(afterTextChangedListener)
            verifyNewPassword.addTextChangedListener(afterTextChangedListener)

            verifyNewPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    resetViewModel.reset(
                        oldPassword.text.toString(),
                        newPassword.text.toString(),

                    )
                }
                false
            }

            reset.setOnClickListener {
                Log.i("Reset Fragment","reset clicked")
                loading.visibility = View.VISIBLE
                resetViewModel.reset(
                    oldPassword.text.toString(),
                    newPassword.text.toString(),

                    )
                Utils.hideSoftKeyBoard(application, it )
            }


        }


        return binding.root
    }
    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = "Password has been reseted " + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showResetFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

}