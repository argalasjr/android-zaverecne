package sk.stuba.fei.mv.android.zaverecne.auth.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val displayName: String,
    val profilePicture: String,
    val email:String,
    //... other data fields that may be accessible to the UI
)