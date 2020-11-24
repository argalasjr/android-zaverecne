package sk.stuba.fei.mv.android.zaverecne.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

private const val BASE_URL = "http://api.mcomputing.eu/mobv/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ApiService {
    @POST("service.php")
    suspend fun registerUser(@Body params: RequestBody): UserResult

    @POST("service.php")
    suspend fun loginUser(@Body params: RequestBody): UserResult

    @POST("service.php")
    suspend fun changeUserPassword(@Body params: RequestBody): UserResult

    @POST("service.php")
    suspend fun refreshUserToken(@Body params: RequestBody): UserResult

    @POST("service.php")
    suspend fun existsUser(@Body params: RequestBody): UserExistsResult

    @POST("service.php")
    suspend fun fetchUserPosts(@Body params: RequestBody): List<UserPostResult>

    @POST("service.php")
    suspend fun fetchUserProfile(@Body params: RequestBody): UserResult

    @Multipart
    @POST("upload.php")
    suspend fun uploadProfilePicture(@Part file: MultipartBody.Part, @Part params: MultipartBody.Part): UserActionResult

    @POST("service.php")
    suspend fun removeProfilePicture(@Body params: RequestBody): UserActionResult

    @Multipart
    @POST("post.php")
    suspend fun uploadPost(@Part file: MultipartBody.Part, @Part params: MultipartBody.Part): UserActionResult

    @POST("service.php")
    suspend fun removePost(@Body params: RequestBody): UserActionResult
}

object Api {
    val retrofitService : ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}