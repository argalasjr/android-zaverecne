package sk.stuba.fei.mv.android.zaverecne.auth.register


/**
 * Authentication result : success (user details) or error message.
 */
data class RegisterResult(
    val success: RegisteredUserView? = null,
    val error: Int? = null
)