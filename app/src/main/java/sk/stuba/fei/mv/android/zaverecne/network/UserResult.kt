package sk.stuba.fei.mv.android.zaverecne.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserResult (
    val id: String,
    val username: String,
    val email: String,
    val token: String,
    val refresh: String,
    val profile: String) : Parcelable {}