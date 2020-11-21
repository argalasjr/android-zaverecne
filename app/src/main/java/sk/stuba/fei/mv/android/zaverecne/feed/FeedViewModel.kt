package sk.stuba.fei.mv.android.zaverecne.feed

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val _posts = MutableLiveData<List<FeedPost>>()

    private val repo = MasterRepository(application)

    val posts: LiveData<List<FeedPost>>
        get() = _posts

//    fun test() {
//        viewModelScope.launch {
//            val resp = repo.existsUser("test1")
//            println(resp)
//        }
//    }
}