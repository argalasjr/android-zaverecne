package sk.stuba.fei.mv.android.zaverecne.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.ApiStatus
import sk.stuba.fei.mv.android.zaverecne.auth.login.LoggedInUserView
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository

class ProfileViewModel(private val masterRepository: MasterRepository)  : ViewModel() {

    private val _status = MutableLiveData<ApiStatus>()

    val status: LiveData<ApiStatus>
        get() = _status

    private val _loggedInUserView = MutableLiveData<LoggedInUserView>()
    val loggedInUserView: LiveData<LoggedInUserView> = _loggedInUserView


    init {

        viewModelScope.launch {

            val user = masterRepository.dbExistsActiveUser()
            _status.value = ApiStatus.LOADING
            if ( user != null ){
                    if(user.profilePicSrc.isEmpty()){
                        user.profilePicSrc = "https://www.csudh.edu/Assets/csudh-sites/slp/images/faculty-staff-photos/nophoto_icon-user-default.jpg"
                    }
               _loggedInUserView.value = LoggedInUserView(
                   displayName = user.userName,
                   profilePicture = user.profilePicSrc,
                   email = user.email)

                _status.value = ApiStatus.DONE
            }else{
                _status.value = ApiStatus.ERROR
            }
        }



    }
}