package sk.stuba.fei.mv.android.zaverecne.repository

import android.content.Context
import okhttp3.RequestBody
import org.json.JSONObject
import sk.stuba.fei.mv.android.zaverecne.database.User
import sk.stuba.fei.mv.android.zaverecne.database.UserDatabase
import sk.stuba.fei.mv.android.zaverecne.database.UserDatabaseDao
import sk.stuba.fei.mv.android.zaverecne.network.Api
import sk.stuba.fei.mv.android.zaverecne.network.UserExistsResult
import sk.stuba.fei.mv.android.zaverecne.network.UserPostResult
import sk.stuba.fei.mv.android.zaverecne.network.UserResult

object MasterRepository {
    private lateinit var userDao: UserDatabaseDao
    private val api = Api

    // TODO: presunut API kluc do .properties suboru
    private val apiKey = "fL5pP7jH4uM0lE6jP2gD0xY2jJ2nL4"

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

    suspend fun dbGetUser(userName: String) : User? {
        return userDao.get(userName)
    }

    suspend fun registerUser(userName: String, password: String, email: String): UserResult {
        val req = createJsonRequestBody(
            "action" to "register",
            "apikey" to apiKey,
            "email" to email,
            "username" to userName,
            "password" to password
        )
        return Api.retrofitService.registerUser(req)
    }

    suspend fun loginUser(userName: String, password: String): UserResult {
        val req = createJsonRequestBody(
            "action" to "login",
            "apikey" to apiKey,
            "username" to userName,
            "password" to password
        )
        return Api.retrofitService.loginUser(req)
    }

    // changes token as well!!
    suspend fun changeUserPassword(oldPassword: String, newPassword: String, token: String): UserResult {
        val req = createJsonRequestBody(
            "action" to "password",
            "apikey" to apiKey,
            "token" to token,
            "oldpassword" to oldPassword,
            "newpassword" to newPassword
        )
        return Api.retrofitService.changeUserPassword(req)
    }

    // changes token as well!!
    suspend fun refreshUserToken(refreshToken: String): UserResult {
        val req = createJsonRequestBody(
            "action" to "refreshToken",
            "apikey" to apiKey,
            "refreshToken" to refreshToken
        )
        return Api.retrofitService.refreshUserToken(req)
    }

    suspend fun existsUser(userName: String): UserExistsResult {
        val req = createJsonRequestBody(
            "action" to "exists",
            "apikey" to apiKey,
            "username" to userName
        )
        return Api.retrofitService.existsUser(req)
    }

    suspend fun fetchUserPosts(token: String): List<UserPostResult> {
        val req = createJsonRequestBody(
            "action" to "posts",
            "apikey" to apiKey,
            "token" to token
        )
        return Api.retrofitService.fetchUserPosts(req)
    }


    // https://stackoverflow.com/questions/21398598/how-to-post-raw-whole-json-in-the-body-of-a-retrofit-request
    private fun createJsonRequestBody(vararg params: Pair<String, String>) =
        RequestBody.create(
            okhttp3.MediaType.parse("application/json"),
            JSONObject(mapOf(*params)).toString()
        )
}