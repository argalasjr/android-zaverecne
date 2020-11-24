package sk.stuba.fei.mv.android.zaverecne.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository
import java.io.*


class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val _posts = MutableLiveData<List<FeedPost>>()

    val posts: LiveData<List<FeedPost>>
        get() = _posts

    private val _status = MutableLiveData<ApiStatus>()

    val status: LiveData<ApiStatus>
        get() = _status

    private val repo = MasterRepository(application)

    private val context = application

//    fun test() {
//        viewModelScope.launch {
//            val resp = repo.registerUser("test1", "test2", "test@t.t")
//            println(resp)
//
//            val resp2 = repo.existsUser("test1")
//            println(resp2)

            // TEST ADDING PROFILE PICTURE
//            val dirName = "Pictures/"
//            val dir: File? = context.getExternalFilesDir(dirName)
//            dir?.let {
//                val fileName = "pic1.jpg"
//                val f: File = dir.resolve(fileName)
//                f.let {
//                    val resp = repo.uploadProfilePicture("54fda9ca921534d3d33c3aa0716af62f", it)
//                    println(resp)
//                }
//            }
            // TEST ADDING VIDEO POST
//            val dirName = "Videos/"
//            val dir: File? = context.getExternalFilesDir(dirName)
//            dir?.let {
//                val fileName = "vid1.mp4"
//                val f: File = dir.resolve(fileName)
//                f.let {
//                    val resp = repo.uploadPost("54fda9ca921534d3d33c3aa0716af62f", it)
//                    println(resp)
//                }
//            }
//
//
//        }
//    }


    init {
        getUserPosts()
//        test()
    }

    private fun getUserPosts() {
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                val userPosts = repo.fetchUserPosts("54fda9ca921534d3d33c3aa0716af62f")
                val feedPosts: ArrayList<FeedPost> = arrayListOf()
                userPosts?.forEach { userPost ->
                    val thumbnail: String = "PLACEHOLDER" // TODO: add video thumbnail
                    val title: String = "PLACEHOLDER" // TODO: add video title
                    val post = FeedPost(userPost.postid, userPost.videourl, thumbnail, title)
                    feedPosts.add(post)
                }
                _posts.value = feedPosts
                _status.value = ApiStatus.DONE
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _posts.value = ArrayList()
            }

        }
    }
}

enum class ApiStatus { LOADING, ERROR, DONE }