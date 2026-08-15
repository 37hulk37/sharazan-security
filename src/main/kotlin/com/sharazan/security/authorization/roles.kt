package com.sharazan.security.authorization

import com.sharazan.core.getContextOrNull
import com.sharazan.security.exception.AccessDeniedException
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.AuthenticationException
import com.sharazan.security.core.Authority
import org.http4k.core.Request

fun Request.hasRole(role: String) {
    val authentication = getContextOrNull<Authentication>("authentication")
        ?: throw AuthenticationException("Authentication not set")

    if (!authentication.authorities().contains(Authority(role))) {
        throw AccessDeniedException("There is no role for $role")
    }
}

fun Request.hasAnyRole(vararg roles: String) {
    val authentication = getContextOrNull<Authentication>("authentication")
        ?: throw AuthenticationException("Authentication not set")

    val authorities = roles.map(::Authority)
    if (authorities.none { it in authentication.authorities() }) {
        throw AccessDeniedException("There is no role for $roles")
    }
}