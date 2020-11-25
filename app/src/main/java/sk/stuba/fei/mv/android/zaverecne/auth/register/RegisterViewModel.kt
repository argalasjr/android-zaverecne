package sk.stuba.fei.mv.android.zaverecne.auth.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.database.User
import sk.stuba.fei.mv.android.zaverecne.network.UserResult

import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository

class RegisterViewModel(private val masterRepository: MasterRepository):ViewModel() {


    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun register(username: String, password: String,email:String ) {

        viewModelScope.launch {

            val result = masterRepository.registerUser(username,password,email)
            if (result != null) {
                _registerResult.value =
                    RegisterResult(success = RegisteredUserView(
                        displayName = result.username,
                        profilePicture = result.profile,
                        email = result.email
                    ))

                masterRepository.dbClearUsers();

                val user = User(
                    result.username,
                    result.email,
                    result.profile,
                    result.token,
                    result.refresh
                )
                masterRepository.dbInsertUser(user)

            } else {
                _registerResult.value = RegisterResult(error = R.string.register_failed)
        }

        }

    }

    fun registerDataChanged(username: String, password: String,email: String, verifyPassword: String) {
        if (!isUserNameValid(username)) {
            _registerForm.value = RegisterFormState(usernameError = R.string.invalid_username)
        }        else if (!isEmailValid(email)) {
        _registerForm.value = RegisterFormState(emailError =  R.string.invalid_email)

        } else if (!isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        }else if (!password.equals(verifyPassword)){
            _registerForm.value = RegisterFormState(verifyPasswordError =  R.string.invalid_verify_password)
        }
        else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    private fun isEmailValid(username: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(username).matches()
    }


    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
    
    
}