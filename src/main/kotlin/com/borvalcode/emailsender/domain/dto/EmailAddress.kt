package com.borvalcode.emailsender.domain.dto

import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

@JvmInline
internal value class EmailAddress private constructor(val value: String) {

    init {
        require(
            try {
                InternetAddress(value).validate()
                true
            } catch (ex: AddressException) {
                false
            }
        ) { "Invalid email address: $value" }
    }

    companion object {
        @JvmStatic
        infix fun of(value: String) = EmailAddress(value)

    }

}




