package sk.stuba.fei.mv.android.zaverecne.feed

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeedPost(var imgSrcUrl: String) : Parcelable {
    //??? cakam na specifikaciu sluzby "posts" z rest API
}