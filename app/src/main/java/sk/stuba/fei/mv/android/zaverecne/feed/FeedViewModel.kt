package sk.stuba.fei.mv.android.zaverecne.feed

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.feed_fragment.*
import kotlinx.coroutines.*
import sk.stuba.fei.mv.android.zaverecne.ApiStatus
import sk.stuba.fei.mv.android.zaverecne.network.UserResult
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository


class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val _posts = MutableLiveData<List<FeedPost>>()

    val posts: LiveData<List<FeedPost>>
        get() = _posts

    private val _status = MutableLiveData<ApiStatus>()

    val status: LiveData<ApiStatus>
        get() = _status

    private val repo = MasterRepository(application)

    private val _userProfile = MutableLiveData<UserResult>()

    val userProfile: LiveData<UserResult>
        get() = _userProfile

    var isNetworkAvailable = MutableLiveData<Boolean>()

    val context: Context = application.applicationContext


//    fun test() {
//        viewModelScope.launch {
//            repo.dbClearUsers()
//            val testUser = User("test1", "test1@test1.com", "", "bb3cec62753730a1eeebbda2b054ca8d", "2055be04deb9e7b5e46c14307bf3e400")
//            repo.dbInsertUser(testUser)
//            val resp = repo.("test1", "test2", "test@t.t")
//            println(resp)

//            val resp2 = repo.existsUser("test1")
//            println(resp2)
//
//             TEST ADDING PROFILE PICTURE
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
//             TEST ADDING VIDEO POST
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
//        test()
        getUserPosts()
        getUserPhotoProfile()
    }

    private fun hasNetworkAvailable(context: Context): Boolean {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager?
        val network = manager?.activeNetworkInfo
        Log.d("classTag", "hasNetworkAvailable: ${(network != null)}")
        return (network != null)
    }

     fun getUserPhotoProfile() {
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                val activeUser = repo.dbExistsActiveUser()
                activeUser?.let {
                    _userProfile.value = repo.fetchUserProfile(activeUser.token)
                    _status.value = ApiStatus.DONE
                }
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
            }

        }
    }

    fun deleteUserPost(view: View?, feedPost: FeedPost){
        viewModelScope.launch { // launch a new coroutine in background and continue
                _status.value = ApiStatus.LOADING
            try {
                val activeUser = MasterRepository.dbExistsActiveUser()
                activeUser?.let {
                    MasterRepository.removePost(activeUser.token, feedPost.postId)
                    val snackbar = Snackbar
                            .make(
                                    view!!,
                                    "The post has been deleted.",
                                    Snackbar.LENGTH_LONG
                            )
                    snackbar.show()
                    _status.value = ApiStatus.DONE

                    val feedPosts = _posts.value
                    feedPosts?.let {
                        val newList : ArrayList<FeedPost> = arrayListOf()
                        newList.addAll(it)
                        newList.remove(feedPost)
                        _posts.value = newList
                    }
                }
            } catch (e: Exception) {
                    _status.value = ApiStatus.ERROR
            }
        }
    }

    fun getUserPosts() {
        getUserPosts(null)
    }

    fun getUserPosts(delayMs: Long?) {
        if(hasNetworkAvailable(context)) {
            viewModelScope.launch {
                if (delayMs != null)
                    delay(delayMs)
                _status.value = ApiStatus.LOADING
                Log.d("status", "loading")

                try {
                    val activeUser = repo.dbExistsActiveUser()
                    activeUser?.let {
                        val userPosts = repo.fetchUserPosts(activeUser.token)
                        val feedPosts: ArrayList<FeedPost> = arrayListOf()
                        userPosts?.forEach { userPost ->
                            val thumbnail: String = "" // TODO: add video thumbnail
                            val title: String = "PLACEHOLDER" // TODO: add video title
                            val post = FeedPost(
                                userPost.postid,
                                userPost.username,
                                userPost.videourl,
                                userPost.created,
                                title,
                                userPost.profile,
                            )
                            feedPosts.add(post)
                        }
                        _posts.value = feedPosts
                        _status.value = ApiStatus.DONE
                        Log.d("status", "done")

                    }

                } catch (e: Exception) {
                    Log.d("status", "error")
                    _status.value = ApiStatus.ERROR
                    _posts.value = ArrayList()
                }
            }
        }else{
            _status.value = ApiStatus.RETRY
            Log.d("status", "retry")

        }
    }


}



