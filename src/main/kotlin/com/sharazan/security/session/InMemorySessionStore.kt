package com.sharazan.security.session

import com.sharazan.security.core.Authentication
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemorySessionStore(
    private val ttl: Duration = Duration.ofMinutes(30),
): SessionStore {

    private val sessions = ConcurrentHashMap<String, Session>()

    override fun create(authentication: Authentication): Session {
        val session = Session(
            id = UUID.randomUUID().toString(),
            authentication = authentication,
            expiresAt = Instant.now().plus(ttl),
        )
        sessions[session.id] = session

        return session
    }

    override fun find(id: String): Session? = sessions[id]

    override fun invalidate(id: String) {
        sessions.remove(id)
    }

}
