package com.sharazan.security.authentication

import com.sharazan.security.core.AccountDetails
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.Authority

class AccountAuthentication(
    private val accountDetails: AccountDetails,
    private val authorities: Set<Authority>,
): Authentication {

    override fun principal(): Any = accountDetails

    override fun credentials(): String = ""

    override fun authorities(): Set<Authority> = authorities

    override fun isAuthenticated(): Boolean = true

}