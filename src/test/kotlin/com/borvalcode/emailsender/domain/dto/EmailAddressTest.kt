package com.borvalcode.emailsender.domain.dto

import org.amshove.kluent.*
import kotlin.test.Test

internal class EmailAddressTest {

    @Test
    fun `should parse a correct email address`() {

        invoking {

            val actual = EmailAddress of "anemail@email.com"
            actual.value `should be equal to` "anemail@email.com"

        } `should not throw` AnyException

    }

    @Test
    fun `should throw an exception when email address is not correct`() {

        invoking {
            EmailAddress of "invalidAddress"
        } `should throw` IllegalArgumentException::class `with message` "Invalid email address: invalidAddress"

    }

}