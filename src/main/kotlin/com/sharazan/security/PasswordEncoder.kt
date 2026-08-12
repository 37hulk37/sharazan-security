package com.sharazan.security

import org.mindrot.jbcrypt.BCrypt

class PasswordEncoder {

    fun encode(rawPassword: String): String =
        BCrypt.hashpw(rawPassword, BCrypt.gensalt())

    fun matches(rawPassword: String, encodedPassword: String): Boolean =
        BCrypt.checkpw(rawPassword, encodedPassword)

}