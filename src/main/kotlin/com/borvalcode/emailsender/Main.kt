package com.borvalcode.emailsender

import com.borvalcode.emailsender.domain.dto.Email.Body.Companion.of
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Html
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Text
import com.borvalcode.emailsender.domain.dto.Email.Companion.email
import com.borvalcode.emailsender.domain.dto.Email.Companion.of
import com.borvalcode.emailsender.domain.dto.EmailError
import com.borvalcode.emailsender.domain.handle
import com.borvalcode.emailsender.infrastructure.DefaultEmailSender
import com.borvalcode.emailsender.infrastructure.`or else`
import com.borvalcode.emailsender.infrastructure.get

fun main() {

    val inputUrl = "input"

    // Safe way of instantiating an EmailSender
    val defaultSender = DefaultEmailSender.of(host = "localhost", port = 25)

    // Non-safe way of instantiating an EmailSender: if you ensure the url is [host]:[port], you can use method get()
    val sender = (DefaultEmailSender on "localhost:25").get()

    // Non-safe way of instantiating an EmailSender: if you can't ensure the url is well-formed, you can declare a default object
    val orDefaultSender = DefaultEmailSender on inputUrl `or else` defaultSender

    // Non-safe way of instantiating an EmailSender: if you can't ensure the url is well-formed, you can declare a routine if parsing fails
    val orThrowSender = DefaultEmailSender on inputUrl `or else` { throw RuntimeException(it.message, it.cause) }

    ////////////////////

    // The simplest email
    val emailMessage = email from "borvalgue@gmail.com" to "veraeikon.b@gmail.com" subject "Hello" body "Hello World"

    // Email with complex body
    val emailWithComplexBody = (email from "borvalgue@gmail.com" to "veraeikon.b@gmail.com" subject "Hello"
            body of(
        Text of "Hello",
        Html of "<html></html>",
        Text of "Good Bye"
    ))

    // Email with attached files
    val emailWithAttached = (email from "borvalgue@gmail.com" to "veraeikon.b@gmail.com" subject "Hello"
            `attached files` of("File1.txt", "File2.txt")
            body (Html of "<html></html>"))


    ////////////////////

    sender send emailMessage

    (orDefaultSender send emailWithAttached)
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: $it") })

    (orThrowSender send emailWithComplexBody)
        .handle(onSuccess, onError)

}

private val onError: (EmailError) -> Unit = { println("Error sending email: $it") }

private val onSuccess: (Unit) -> Unit = { println("Mail sent!") }
