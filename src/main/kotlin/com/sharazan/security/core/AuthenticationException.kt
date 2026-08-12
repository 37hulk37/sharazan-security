package com.sharazan.security.core

import com.sharazan.http.error.ApiException
import org.http4k.core.Status

open class AuthenticationException(
    message: String,
    cause: Throwable? = null
): ApiException(message, Status.UNAUTHORIZED) {

    init {
        cause?.let { initCause(it) }
    }

}
