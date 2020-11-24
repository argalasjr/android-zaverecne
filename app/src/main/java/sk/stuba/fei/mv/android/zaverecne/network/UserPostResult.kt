package sk.stuba.fei.mv.android.zaverecne.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserPostResult (
    val postid: String,
    val created: String,
    val videourl: String,
    val username: String,
    val profile: String) : Parcelable {}