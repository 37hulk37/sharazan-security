package com.sharazan.security.authentication.jwt

import com.sharazan.security.configuration.JwtProperties
import com.sharazan.security.core.AccountDetails
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.util.*

class SimpleJwtParser(
    properties: JwtProperties,
): JwtParser {

    private val secretKey = Keys.hmacShaKeyFor(properties.secret.toByteArray())
    private val expiration = properties.expiration.toLong()


    override fun parseToken(token: String): Token {
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        val roles = claims.get("roles", List::class.java)
            .filterIsInstance<String>()

        return Token(
            subject = claims.subject,
            expireAt = claims.expiration.toInstant(),
            roles = roles,
        )
    }

    override fun create(details: AccountDetails): String {
        val now = Instant.now()

        return Jwts.builder()
            .subject(details.username())
            .claim("roles", details.authorities().toList())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expiration * 60)))
            .signWith(secretKey)
            .compact()
    }

}