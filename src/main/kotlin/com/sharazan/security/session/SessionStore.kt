package com.sharazan.security.session

import com.sharazan.security.core.Authentication

interface SessionStore {

    fun create(authentication: Authentication): Session

    fun find(id: String): Session?

    fun invalidate(id: String)

}
