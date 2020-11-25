package sk.stuba.fei.mv.android.zaverecne.repository

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonDataException
import okhttp3.Headers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException
import sk.stuba.fei.mv.android.zaverecne.database.User
import sk.stuba.fei.mv.android.zaverecne.database.UserDatabase
import sk.stuba.fei.mv.android.zaverecne.database.UserDatabaseDao
import sk.stuba.fei.mv.android.zaverecne.network.*
import java.io.File
import java.net.ProtocolException


object MasterRepository {
    private lateinit var userDao: UserDatabaseDao
    private val api = Api

    // TODO: presunut API kluc do .properties suboru
    private val apiKey = "fL5pP7jH4uM0lE6jP2gD0xY2jJ2nL4"
    private val _mediaUrlBase: String = "http://api.mcomputing.eu/mobv/uploads/"

    private var numRetries: Int = 0

    val mediaUrlBase: String
            get() = _mediaUrlBase

    operator fun invoke(context: Context): MasterRepository {
        userDao = UserDatabase.getInstance(context).userDatabaseDao
        return this
    }

    suspend fun dbInsertUser(user: User) {
        userDao.insert(user)
    }

    suspend fun dbUpdateUser(user: User) {
        userDao.update(user)
    }

    suspend fun dbDeleteUser(user: User) {
        userDao.delete(user)
    }

    suspend fun dbExistsActiveUser(): User? {
        return userDao.getActive()
    }

    suspend fun dbClearUsers() {
        userDao.clear()
    }

    suspend fun registerUser(userName: String, password: String, email: String): UserResult? {
        val req = createJsonRequestBody(
            "action" to "register",
            "apikey" to apiKey,
            "email" to email,
            "username" to userName,
            "password" to password
        )
        return Api.retrofitService.runCatching {
            registerUser(req)
        }.getOrElse {
            handleException("registerUser", it)
            null
        }
    }

    suspend fun loginUser(userName: String, password: String): UserResult? {
        val req = createJsonRequestBody(
            "action" to "login",
            "apikey" to apiKey,
            "username" to userName,
            "password" to password
        )
        return Api.retrofitService.runCatching {
            loginUser(req)
        }.getOrElse {
            handleException("loginUser", it)
            null
        }
    }

    // changes token as well!!
    suspend fun changeUserPassword(oldPassword: String, newPassword: String, token: String): UserResult? {
        val req = createJsonRequestBody(
            "action" to "password",
            "apikey" to apiKey,
            "token" to token,
            "oldpassword" to oldPassword,
            "newpassword" to newPassword
        )
        Api.retrofitService.runCatching {
            val res = changeUserPassword(req)
            numRetries = 0
            return res
        }.getOrElse {
            val err = handleException("changeUserPassword", it)
            if(err == 401 && numRetries < 3) {
                // user token not valid anymore, refresh and try again
                val newToken = renewUserToken()
                newToken?.let {
                    numRetries += 1
                    return changeUserPassword(oldPassword, newPassword, newToken)
                }
            }
            return null
        }
    }

    // changes token as well!!
    suspend fun refreshUserToken(refreshToken: String): UserResult? {
        val req = createJsonRequestBody(
            "action" to "refreshToken",
            "apikey" to apiKey,
            "refreshToken" to refreshToken
        )
        return Api.retrofitService.runCatching {
            refreshUserToken(req)
        }.getOrElse {
            handleException("refreshUserToken", it)
            null
        }
    }

    suspend fun existsUser(userName: String): UserExistsResult? {
        val req = createJsonRequestBody(
            "action" to "exists",
            "apikey" to apiKey,
            "username" to userName
        )
        return Api.retrofitService.runCatching {
            existsUser(req)
        }.getOrElse {
            handleException("existsUser", it)
            null
        }
    }

    suspend fun fetchUserPosts(token: String): List<UserPostResult>? {
        val req = createJsonRequestBody(
            "action" to "posts",
            "apikey" to apiKey,
            "token" to token
        )
        Api.retrofitService.runCatching {
            val res = fetchUserPosts(req)
            numRetries = 0
            return res
        }.getOrElse {
            val err = handleException("fetchUserPosts", it)
            if(err == 401 && numRetries < 3) {
                // user token not valid anymore, refresh and try again
                val newToken = renewUserToken()
                newToken?.let {
                    numRetries += 1
                    return fetchUserPosts(newToken)
                }
            }
            return null
        }
    }

    suspend fun fetchUserProfile(token: String): UserResult? {
        val req = createJsonRequestBody(
            "action" to "userProfile",
            "apikey" to apiKey,
            "token" to token
        )
        Api.retrofitService.runCatching {
            val res = fetchUserProfile(req)
            numRetries = 0
            return res
        }.getOrElse {
            val err = handleException("fetchUserProfile", it)
            if(err == 401 && numRetries < 3) {
                // user token not valid anymore, refresh and try again
                val newToken = renewUserToken()
                newToken?.let {
                    numRetries += 1
                    return fetchUserProfile(newToken)
                }
            }
            return null
        }
    }

    suspend fun uploadProfilePicture(token: String, toUpload: File): UserActionResult? {
        val dataReq = createJsonRequestBody(
            "apikey" to apiKey,
            "token" to token
        )
        val imageReq = createImageRequestBody(toUpload)

        val imagePart = MultipartBody.Part.create(
            Headers.of(
                "Content-Disposition",
                "form-data; name=\"image\"; filename=\"" + toUpload.name + "\""
            ),
            imageReq
        )

        val dataPart = MultipartBody.Part.create(
            Headers.of(
                "Content-Disposition",
                "form-data; name=\"data\""
            ),
            dataReq
        )
        Api.retrofitService.runCatching {
            val res = uploadProfilePicture(imagePart, dataPart)
            numRetries = 0
            return res
        }.getOrElse {
            val err = handleException("uploadProfilePicture", it)
            if(err == 401 && numRetries < 3) {
                // user token not valid anymore, refresh and try again
                val newToken = renewUserToken()
                newToken?.let {
                    numRetries += 1
                    return uploadProfilePicture(newToken, toUpload)
                }
            }
            return null
        }
    }

    suspend fun removeProfilePicture(token: String): UserActionResult? {
        val req = createJsonRequestBody(
            "action" to "clearPhoto",
            "apikey" to apiKey,
            "token" to token
        )
        Api.retrofitService.runCatching {
            val res = removeProfilePicture(req)
            numRetries = 0
            return res
        }.getOrElse {
            val err = handleException("removeProfilePicture", it)
            if(err == 401 && numRetries < 3) {
                // user token not valid anymore, refresh and try again
                val newToken = renewUserToken()
                newToken?.let {
                    numRetries += 1
                    return removeProfilePicture(newToken)
                }
            }
            return null
        }
    }

    suspend fun uploadPost(token: String, toUpload: File): UserActionResult? {
        val dataReq = createJsonRequestBody(
            "apikey" to apiKey,
            "token" to token
        )
        val videoReq = createVideoRequestBody(toUpload)

        val videoPart = MultipartBody.Part.create(
            Headers.of(
                "Content-Disposition",
                "form-data; name=\"video\"; filename=\"" + toUpload.name + "\""
            ),
            videoReq
        )

        val dataPart = MultipartBody.Part.create(
            Headers.of(
                "Content-Disposition",
                "form-data; name=\"data\""
            ),
            dataReq
        )
        Api.retrofitService.runCatching {
            val res = uploadPost(videoPart, dataPart)
            numRetries = 0
            return res
        }.getOrElse {
            val err = handleException("uploadPost", it)
            if(err == 401 && numRetries < 3) {
                // user token not valid anymore, refresh and try again
                val newToken = renewUserToken()
                newToken?.let {
                    numRetries += 1
                    return uploadPost(newToken, toUpload)
                }
            }
            return null
        }
    }

    suspend fun removePost(token: String, postId: String): UserActionResult? {
        val req = createJsonRequestBody(
            "action" to "deletePost",
            "apikey" to apiKey,
            "token" to token,
            "id" to postId
        )
        Api.retrofitService.runCatching {
            val res = removePost(req)
            numRetries = 0
            return res
        }.getOrElse {
            val err = handleException("removePost", it)
            if(err == 401 && numRetries < 3) {
                // user token not valid anymore, refresh and try again
                val newToken = renewUserToken()
                newToken?.let {
                    numRetries += 1
                    return removePost(newToken, postId)
                }
            }
            return null
        }
    }

    private suspend fun renewUserToken(): String? {
        val activeUser = dbExistsActiveUser()
        activeUser?.let {
            val res = refreshUserToken(activeUser.refreshToken)
            res?.let {
                val updatedUser = User(activeUser.userName, activeUser.email, activeUser.profilePicSrc, res.token, res.refresh)
                dbUpdateUser(updatedUser)
                return res.token
            }
        }
        return null
    }


    // https://stackoverflow.com/questions/21398598/how-to-post-raw-whole-json-in-the-body-of-a-retrofit-request
    private fun createJsonRequestBody(vararg params: Pair<String, String>) =
        RequestBody.create(
            okhttp3.MediaType.parse("application/json"),
            JSONObject(mapOf(*params)).toString()
        )

    private fun createVideoRequestBody(file: File) =
        RequestBody.create(
            okhttp3.MediaType.parse("video/mp4"),
            file
        )

    private fun createImageRequestBody(file: File) =
        RequestBody.create(
            okhttp3.MediaType.parse("image/jpeg"),
            file
        )

    private fun handleException(fn: String, e: Throwable): Int {
        when (e) {
            is HttpException -> {
                Log.e(fn, "HTTP error. Function $fn failed with exception: ${e.javaClass.canonicalName} and message: ${e.message()}")
                return e.code()
            }
            is ProtocolException -> {
                Log.e(fn, "Protocol error. Function $fn failed with exception: ${e.javaClass.canonicalName}.")
            }
            is JsonDataException -> {
                Log.e(fn, "JSON data error. Function $fn failed with exception: ${e.javaClass.canonicalName} and message: ${e.message}")
            }
            else -> Log.e(fn, "Error. Function $fn failed with exception: ${e.javaClass.canonicalName} and message: ${e.message}")
        }
        return 0
    }
}