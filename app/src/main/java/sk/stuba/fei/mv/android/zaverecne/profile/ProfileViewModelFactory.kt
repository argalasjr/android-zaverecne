package sk.stuba.fei.mv.android.zaverecne.profile


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class ProfileViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                masterRepository = MasterRepository(application)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}