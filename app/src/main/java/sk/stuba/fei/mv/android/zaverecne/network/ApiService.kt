package sk.stuba.fei.mv.android.zaverecne.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

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
}

object Api {
    val retrofitService : ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}