package sk.stuba.fei.mv.android.zaverecne.auth.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.database.User
import sk.stuba.fei.mv.android.zaverecne.network.Result
import sk.stuba.fei.mv.android.zaverecne.network.UserResult
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository

class LoginViewModel(private val masterRepository: MasterRepository) : ViewModel() {



    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult


    init {

        viewModelScope.launch {
            //masterRepository.dbClearUsers()
            val user = masterRepository.dbExistsActiveUser()

            if ( user != null ){
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(
                        displayName = user.userName,
                        profilePicture = user.profilePicSrc,
                        email = user.email
                    ))
            }

        }


        
    }

    fun login(username: String, password: String) {

        viewModelScope.launch {

            val result = masterRepository.loginUser(username,password)


            if(result != null){
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(
                        displayName = result.username,
                        profilePicture = result.profile,
                        email = result.email))

                masterRepository.dbClearUsers()
                val user = User(
                    result.username,
                    result.email,
                    result.profile,
                    result.token,
                    result.refresh
                )
                masterRepository.dbInsertUser(user)



            } else {
                _loginResult.value = LoginResult(error = R.string.auth_failed)
            }



        }

    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
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