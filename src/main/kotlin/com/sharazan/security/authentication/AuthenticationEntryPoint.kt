package com.sharazan.security.authentication

import com.sharazan.security.core.AuthenticationException
import org.http4k.core.Request

interface AuthenticationEntryPoint {

    fun commence(request: Request, exception: AuthenticationException)

}