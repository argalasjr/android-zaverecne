package sk.stuba.fei.mv.android.zaverecne.profile

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.ApiStatus
import sk.stuba.fei.mv.android.zaverecne.auth.login.LoggedInUserView
import sk.stuba.fei.mv.android.zaverecne.network.UserResult
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository
import java.io.File

class ProfileImageViewModel(private val masterRepository: MasterRepository)  : ViewModel() {

    private val _status = MutableLiveData<ApiStatus>()

    val status: LiveData<ApiStatus>
        get() = _status

    private val _loggedInUserView = MutableLiveData<UserResult>()
    val loggedInUserView: LiveData<UserResult> = _loggedInUserView

    init {
        getUser()
    }

    fun getUser() {
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                val activeUser = masterRepository.dbExistsActiveUser()
                activeUser?.let {
                    _loggedInUserView.value = masterRepository.fetchUserProfile(activeUser.token)
                    _status.value = ApiStatus.DONE
                }
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
            }

        }
    }

}