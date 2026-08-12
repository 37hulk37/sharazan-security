package com.sharazan.security.core

interface Authentication {

    fun principal(): Any

    fun credentials(): String

    fun authorities(): Set<Authority>

    fun isAuthenticated(): Boolean

}