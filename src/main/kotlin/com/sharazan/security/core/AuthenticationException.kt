package com.sharazan.security.core

import com.sharazan.core.exception.ApplicationException
import org.http4k.core.Status

open class AuthenticationException(
    message: String,
    cause: Throwable? = null,
    status: Status = Status.UNAUTHORIZED
): ApplicationException(message, cause, status) {

    constructor(cause: Throwable):
            this(cause.message ?: cause.javaClass.simpleName, cause)

}
