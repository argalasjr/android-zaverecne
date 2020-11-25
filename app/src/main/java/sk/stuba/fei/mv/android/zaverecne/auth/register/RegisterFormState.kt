package sk.stuba.fei.mv.android.zaverecne.auth.register

/**
 * Data validation state of the login form.
 */
data class RegisterFormState(
    val usernameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val verifyPasswordError: Int? = null,
    val isDataValid: Boolean = false
)