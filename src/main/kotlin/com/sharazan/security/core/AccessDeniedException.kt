package com.sharazan.security.core

open class AccessDeniedException(
    message: String,
    cause: Throwable? = null,
): AuthenticationException(message, cause)
