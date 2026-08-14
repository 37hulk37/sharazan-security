package com.sharazan.security.authentication.jwt

import com.sharazan.security.core.AccountDetails

class SimpleJwtParser: JwtParser {

    override fun parseToken(token: String): Token {
        TODO("Not yet implemented")
    }

    override fun create(details: AccountDetails): String {
        TODO("Not yet implemented")
    }

}