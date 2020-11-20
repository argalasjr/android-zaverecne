package com.example.helloworld.ui.login

import sk.stuba.fei.mv.android.zaverecne.auth.login.LoggedInUserView

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)