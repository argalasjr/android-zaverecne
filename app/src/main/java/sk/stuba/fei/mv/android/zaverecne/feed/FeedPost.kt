package sk.stuba.fei.mv.android.zaverecne.feed

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class FeedPost(val postId: String,val username: String, val videoSrc: String, val created: String, val title: String, val profile: String) {
}