package com.borvalcode.emailsender.domain.dto

import org.amshove.kluent.*
import org.junit.jupiter.api.Test

internal class EmailServerTest {

    @Test
    fun `should parse a correct host and port`() {

        invoking {
            val actual = EmailServer of "localhost:8080"
            val expected = EmailServer("localhost", 8080)

            actual `should be equal to` expected

        } `should not throw` AnyException
    }

    @Test
    fun `should return a ParseException if it doesn't match with pattern`() {

        invoking {
            EmailServer of "invalid"
        } `should throw` ParseException::class `with message` "Pattern must be [host]:[port], wrong invalid"

        invoking {
            EmailServer of "localhost:local"
        } `should throw` ParseException::class `with message` "Pattern must be [host]:[port], wrong localhost:local"

    }

}

internal typealias ParseException = IllegalArgumentException