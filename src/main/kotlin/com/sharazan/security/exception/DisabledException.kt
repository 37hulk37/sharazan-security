package com.sharazan.security.exception

import com.sharazan.security.core.AuthenticationException
import org.http4k.core.Status

class DisabledException(
    message: String,
    cause: Throwable? = null
): AuthenticationException(message, cause, Status.FORBIDDEN)