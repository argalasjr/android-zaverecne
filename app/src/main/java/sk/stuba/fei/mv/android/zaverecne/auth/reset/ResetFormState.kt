package sk.stuba.fei.mv.android.zaverecne.auth.reset

/**
 * Data validation state of the login form.
 */
data class ResetFormState(
    val oldPasswordError: Int? = null,
    val newPasswordError: Int? = null,
    val verifyNewPasswordError: Int? = null,
    val isDataValid: Boolean = false
)