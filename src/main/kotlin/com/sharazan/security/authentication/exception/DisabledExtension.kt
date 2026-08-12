package com.sharazan.security.authentication.exception

import com.sharazan.security.core.AuthenticationException

class DisabledExtension(
    message: String,
    cause: Throwable? = null
): AuthenticationException(message, cause)