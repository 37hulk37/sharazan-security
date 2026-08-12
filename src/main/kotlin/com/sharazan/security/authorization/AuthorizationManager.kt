package com.sharazan.security.authorization

import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import com.sharazan.security.core.AccessDeniedException
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.AuthenticationException
import com.sharazan.security.core.Authority
import org.http4k.core.Request

class AuthorizationManager(
    private val endpointRegistry: SecurityEndpointRegistry,
) {

    fun authorize(authentication: Authentication, request: Request): Authentication {
        val endpoint = endpointRegistry.getEndpoint(request)
            ?: return authentication

        if (!endpoint.isSecured) {
            return authentication
        }

        if (!authentication.isAuthenticated()) {
            throw AuthenticationException("Authentication is required to access ${request.uri.path}")
        }

        if (!hasRequiredRole(authentication, endpoint.requiredRoles)) {
            throw AccessDeniedException("Missing required role for ${request.uri.path}")
        }

        return authentication
    }

    private fun hasRequiredRole(authentication: Authentication, requiredRoles: Set<Authority>): Boolean {
        if (requiredRoles.isEmpty()) {
            return true
        }

        return requiredRoles.any { role ->
            role in authentication.authorities()
        }
    }

}
