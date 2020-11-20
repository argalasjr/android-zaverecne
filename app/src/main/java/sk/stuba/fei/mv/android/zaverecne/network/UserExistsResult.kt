package sk.stuba.fei.mv.android.zaverecne.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserExistsResult (val exists: String) : Parcelable {}