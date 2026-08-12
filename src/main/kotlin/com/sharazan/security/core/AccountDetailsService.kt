package com.sharazan.security.core

interface AccountDetailsService {

    fun getUser(username: String): AccountDetails

}