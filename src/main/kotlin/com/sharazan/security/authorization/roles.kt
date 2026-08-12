package com.sharazan.security.authorization

import com.sharazan.core.getContext
import com.sharazan.security.core.AccessDeniedException
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.Authority
import org.http4k.core.Request

fun Request.hasRole(role: String) {
    val authentication = getContext<Authentication>("authentication")

    if (!authentication.authorities().contains(Authority(role))) {
        throw AccessDeniedException("There is no role for $role")
    }
}

fun Request.hasAnyRole(vararg roles: String) {
    val authentication = getContext<Authentication>("authentication")

    val authorities = roles.map(::Authority)
    if (!authentication.authorities().containsAll(authorities)) {
        throw AccessDeniedException("There is no role for $roles")
    }
}