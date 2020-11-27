package sk.stuba.fei.mv.android.zaverecne.auth.reset

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.auth.login.LoggedInUserView
import sk.stuba.fei.mv.android.zaverecne.auth.login.LoginResult
import sk.stuba.fei.mv.android.zaverecne.database.User
import sk.stuba.fei.mv.android.zaverecne.network.UserResult

import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository

class ResetViewModel(private val masterRepository: MasterRepository):ViewModel() {


    private val _resetForm = MutableLiveData<ResetFormState>()
    val resetFormState: LiveData<ResetFormState> = _resetForm

    private val _resetResult = MutableLiveData<LoginResult>()
    val resetResult: LiveData<LoginResult> = _resetResult

    fun reset( oldpassword: String,newPassword:String ) {

        viewModelScope.launch {
            val user = masterRepository.dbExistsActiveUser()
            if ( user != null ){
                val result = masterRepository.changeUserPassword(oldpassword,newPassword,user.token);
                if (result != null) {
                    _resetResult.value =
                        LoginResult(success = LoggedInUserView(
                            displayName = result.username,
                            profilePicture = result.profile,
                            email = result.email
                        )
                        )



                    val user = User(
                        result.username,
                        result.email,
                        result.profile,
                        result.token,
                        result.refresh
                    )
                    masterRepository.dbUpdateUser(user)
            }  else {
                    _resetResult.value = LoginResult(error = R.string.change_password_fail)
                }




            } else {
                _resetResult.value = LoginResult(error = R.string.auth_failed)
            }

        }

    }

    fun resetDataChanged(oldPassword: String, newPassword: String, verifyNewPassword: String) {
        if (!isPasswordValid(oldPassword)) {
            _resetForm.value = ResetFormState(oldPasswordError = R.string.invalid_password)
        }      else if (!isPasswordValid(newPassword)) {
            _resetForm.value = ResetFormState(newPasswordError = R.string.invalid_password)
        }else if (!newPassword.equals(verifyNewPassword)){
            _resetForm.value = ResetFormState(verifyNewPasswordError =  R.string.invalid_verify_password)
        }
        else {
            _resetForm.value = ResetFormState(isDataValid = true)
        }
    }




    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }


}