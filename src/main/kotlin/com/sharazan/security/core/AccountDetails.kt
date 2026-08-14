package com.sharazan.security.core

interface AccountDetails {

    fun username(): String

    fun password(): String

    fun authorities(): Set<String>

    fun enabled(): Boolean

    fun accountNotLocked(): Boolean

    fun accountNotExpired(): Boolean
}