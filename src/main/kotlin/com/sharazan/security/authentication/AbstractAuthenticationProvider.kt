package com.sharazan.security.authentication

import com.sharazan.security.authentication.exception.DisabledExtension
import com.sharazan.security.core.AccountDetails
import com.sharazan.security.core.AuthenticationException

abstract class AbstractAuthenticationProvider: AuthenticationProvider {

    protected fun check(details: AccountDetails) {
        if (!details.enabled()) {
            throw DisabledExtension("account is disabled")
        }

        if (!details.accountNotLocked()) {
            throw AuthenticationException("Account is locked")
        }

        if (!details.accountNotExpired()) {
            throw AuthenticationException("Account is locked")
        }
    }

}