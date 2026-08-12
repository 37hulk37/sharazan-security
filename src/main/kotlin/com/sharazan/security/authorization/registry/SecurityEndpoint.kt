package com.sharazan.security.authorization.registry

import com.sharazan.http.core.Route
import com.sharazan.security.core.Authority

data class SecurityEndpoint(
    val route: Route,
    val isSecured: Boolean,
    val requiredRoles: Set<Authority> = emptySet(),
)
