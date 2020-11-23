package sk.stuba.fei.mv.android.zaverecne.feed

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class FeedPost(val postId: Int, val videoSrc: String, val thumbnailSrc: String, val title: String) {
}