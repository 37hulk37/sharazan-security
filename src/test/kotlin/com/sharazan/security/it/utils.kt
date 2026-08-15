package com.sharazan.security.it

import com.sharazan.core.exception.ApplicationException
import com.sharazan.security.authentication.jwt.SimpleJwtService
import com.sharazan.security.configuration.JwtProperties
import com.sharazan.security.core.AccountDetails
import com.sharazan.security.core.AccountDetailsService

fun createJwt(
    secret: String = "very   very        long    and securable secret",
    expiration: Long = 20,
    details: AccountDetails
) = jwtService(secret, expiration)
    .create(details)


fun jwtService(
    secret: String = "very   very        long    and securable secret",
    expiration: Long = 20,
) = SimpleJwtService(JwtProperties(secret, expiration))


fun accountDetails(
    username: String = "test",
    password: String = "password",
    authorities: Set<String> = setOf(),
    enabled: Boolean = true,
    accountNotLocked: Boolean = true,
    accountNotExpired: Boolean = true,
) = object : AccountDetails {

    override fun username(): String = username

    override fun password(): String = password

    override fun authorities(): Set<String> = authorities

    override fun enabled(): Boolean = enabled

    override fun accountNotLocked(): Boolean = accountNotLocked

    override fun accountNotExpired(): Boolean = accountNotExpired

}


fun accountDetailsService(accounts: List<AccountDetails> = emptyList())
    = object : AccountDetailsService {

    override fun getUser(username: String): AccountDetails =
        accounts.firstOrNull { it.username() == username }
            ?: throw ApplicationException("No account for username '$username'")
}