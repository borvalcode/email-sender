package com.borvalcode.emailsender

import arrow.core.flatMap
import com.borvalcode.emailsender.domain.`or else`
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Html
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Text
import com.borvalcode.emailsender.domain.dto.Email.Companion.email
import com.borvalcode.emailsender.domain.dto.EmailError
import com.borvalcode.emailsender.domain.get
import com.borvalcode.emailsender.domain.handle
import com.borvalcode.emailsender.domain.send
import com.borvalcode.emailsender.infrastructure.EmailSender

fun main() {

    val inputUrl = "input"

    // Safe way of instantiating an EmailSender
    val defaultSender = EmailSender.of(host = "localhost", port = 25)

    // Non-safe way of instantiating an EmailSender: if you ensure the url is [host]:[port], you can use method get()
    val sender = (EmailSender on "localhost:25").get()

    // Non-safe way of instantiating an EmailSender: if you can't ensure the url is well-formed, you can declare a default object
    val orDefaultSender = EmailSender on inputUrl `or else` defaultSender

    // Non-safe way of instantiating an EmailSender: if you can't ensure the url is well-formed, you can declare a routine if parsing fails
    val orThrowSender = EmailSender on inputUrl `or else` { throw RuntimeException(it.message, it.cause) }

    ////////////////////

    // The simplest email
    val emailMessage = email from "sender@email.com" to "receiver@email.com" subject "Hello" body "Hello World"

    // Email is also an either, so you can work with it


    // Email with many receivers
    val emailWithReceivers = email from "sender@email.com" to setOf(
        "receiver@email.com",
        "another@gmail.com"
    ) subject "Hello" body "Hello World" `or else` { throw RuntimeException(it.message, it.cause) }

    // Email with complex body
    val emailWithComplexBody = (email from "sender@email.com" to "receiver@email.com" subject "Hello"
            body listOf(
        Text of "Hello",
        Html of "<html></html>",
        Text of "Good Bye"
    ))

    // Email with attached files
    val emailWithAttached = (email from "sender@email.com" to "receiver@email.com" subject "Hello"
            `attached files` setOf("File1.txt", "File2.txt")
            body (Html of "<html></html>"))

    // Email is also non-safe
    email from "from" to "to" subject "subject" body "body" `or else` {
        throw RuntimeException(
            it.message,
            it.cause
        )
    }

    ////////////////////

    sender send emailMessage

    (orDefaultSender send emailWithAttached)
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: $it") })

    (orThrowSender send emailWithComplexBody)
        .handle(onSuccess, onError)

    // Single line integration

    // Wrong url -> Error sending email: Pattern must be [host]:[port]
    (EmailSender on "wrong_url")
        .flatMap { it send (email from "from@from.com" to "to@to.com" subject "subject" body "body") }
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: ${it.message}") })

    // Wrong email address -> Error sending email: Invalid email address: from
    (EmailSender on "localhost:25")
        .flatMap { it send (email from "from" to "to@to.com" subject "subject" body "body") }
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: ${it.message}") })

    // Error when sending -> Error sending email: Error sending email
    (EmailSender on "localhost:25")
        .flatMap { it send (email from "from@from.com" to "to@to.com" subject "subject" body "body") }
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: ${it.message}") })

    // Simplest one line
    (EmailSender.of("host", 25) send (email from "from@from.com" to "to@to.com" subject "subject" body "body"))
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: ${it.message}") })

    ((EmailSender on "wrong_url") send (email from "from@from.com" to "to@to.com" subject "subject" body "body"))
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: ${it.message}") })
}

private val onError: (EmailError) -> Unit = { println("Error sending email: $it") }

private val onSuccess: (Unit) -> Unit = { println("Mail sent!") }
