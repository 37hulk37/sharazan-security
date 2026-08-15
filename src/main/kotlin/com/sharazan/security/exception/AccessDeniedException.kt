package com.sharazan.security.exception

import com.sharazan.security.core.AuthenticationException
import org.http4k.core.Status

open class AccessDeniedException(
    message: String,
    cause: Throwable? = null,
): AuthenticationException(message, cause, Status.UNAUTHORIZED)