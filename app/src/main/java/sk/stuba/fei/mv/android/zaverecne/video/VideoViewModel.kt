package sk.stuba.fei.mv.android.zaverecne.video

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.ApiStatus
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.fetchfiles.FetchFiles.getFileSize
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository
import java.io.File

class VideoViewModel(private val masterRepository: MasterRepository) : ViewModel() {

    private val _status = MutableLiveData<ApiStatus>()

    val status: LiveData<ApiStatus>
        get() = _status


    init {

    }


    fun uploadVideo(view: View?, videoPath: String){
        val size = getFileSize(File(videoPath), "MB")

        if(size <= 8) {
            viewModelScope.launch { // launch a new coroutine in background and continue
                _status.value = ApiStatus.LOADING
                try {
                    val activeUser = masterRepository.dbExistsActiveUser()
                    activeUser?.let {
                        masterRepository.uploadPost(activeUser.token, File(videoPath))
                        val snackbar = Snackbar
                            .make(
                                view!!,
                                "The video has been uploaded succesfully.",
                                Snackbar.LENGTH_LONG
                            )
                        snackbar.show()
                    }
                } catch (e: Exception) {
                    _status.value = ApiStatus.ERROR
                }
            }
        }else{
            val snackbar = Snackbar
                .make(
                    view!!,
                    "The maximum file size is 8.",
                    Snackbar.LENGTH_LONG
                )
            snackbar.show()
        }
    }

}