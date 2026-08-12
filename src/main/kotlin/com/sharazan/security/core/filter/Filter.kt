package com.sharazan.security.core.filter

interface Filter<T> {

    fun isBefore(): Boolean

    fun doFilter(r: T): T

}