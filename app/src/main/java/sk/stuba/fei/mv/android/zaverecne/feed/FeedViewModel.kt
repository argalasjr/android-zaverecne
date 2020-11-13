package sk.stuba.fei.mv.android.zaverecne.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FeedViewModel : ViewModel() {
    private val _posts = MutableLiveData<List<FeedPost>>()

    val posts: LiveData<List<FeedPost>>
        get() = _posts
}