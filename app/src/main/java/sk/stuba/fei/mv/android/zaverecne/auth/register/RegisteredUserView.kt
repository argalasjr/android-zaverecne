package sk.stuba.fei.mv.android.zaverecne.auth.register

/**
 * User details post authentication that is exposed to the UI
 */
data class RegisteredUserView(
    val displayName: String,
    val profilePicture: String,
    val email:String,
    //... other data fields that may be accessible to the UI
)