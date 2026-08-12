package com.sharazan.security.authentication.anonymous

import com.sharazan.security.core.Authority
import com.sharazan.security.core.Authentication

class AnonymousAuthentication: Authentication {

    override fun principal(): Any = "anonymous"

    override fun credentials(): String = "anonymous"

    override fun authorities(): Set<Authority> = emptySet()

    override fun isAuthenticated(): Boolean = false

}
