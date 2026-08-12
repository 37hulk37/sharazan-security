package com.sharazan.security.authentication.login

import com.sharazan.security.core.Authority
import com.sharazan.security.core.Authentication

data class UsernamePasswordAuthentication(
    val username: String,
    val password: String,
    val authorities: Set<Authority> = emptySet(),
): Authentication {

    override fun principal() = username

    override fun credentials() = password

    override fun authorities(): Set<Authority> = authorities

    override fun isAuthenticated(): Boolean = false

}
