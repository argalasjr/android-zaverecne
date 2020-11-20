package sk.stuba.fei.mv.android.zaverecne.auth.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.network.UserResult

import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository

class RegisterViewModel(private val masterRepository: MasterRepository):ViewModel() {


    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun register(username: String, password: String,email:String ) {

        viewModelScope.launch {

            try {
                val result = masterRepository.registerUser(username,password,email)
                if (result is UserResult) {
                    _registerResult.value =
                        RegisterResult(success = RegisteredUserView(displayName = result.username))
                } else {
                    _registerResult.value = RegisterResult(error = R.string.auth_failed)
                }
            } catch (e: Exception) {
                _registerResult.value = RegisterResult(error = R.string.auth_failed)
            }



        }

    }

    fun registerDataChanged(username: String, password: String,email: String) {
        if (!isUserNameValid(username)) {
            _registerForm.value = RegisterFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if (!isUserNameValid(email)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
    } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
    
    
}