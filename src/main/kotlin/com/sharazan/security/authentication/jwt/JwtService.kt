package com.sharazan.security.authentication.jwt

import com.sharazan.security.core.AccountDetails

interface JwtService {

    fun parseToken(token: String): Token

    fun create(details: AccountDetails): String

}