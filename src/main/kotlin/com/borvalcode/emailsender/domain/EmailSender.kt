package com.borvalcode.emailsender.domain

import arrow.core.Either
import arrow.core.flatMap
import com.borvalcode.emailsender.domain.dto.Email
import com.borvalcode.emailsender.domain.dto.EmailError

interface EmailSender {
    infix fun send(either: Either<EmailError, Email>): Either<EmailError, Unit>
}

infix fun Either<EmailError, EmailSender>.send(email: Either<EmailError, Email>) = this.flatMap { it send email }

fun <A> Either<EmailError, Unit>.handle(success: (Unit) -> A, error: (EmailError) -> A) =
    this.fold(ifLeft = error, ifRight = success)

infix fun <A, B> Either<A, B>.`or else`(onError: (A) -> B) = this.fold(ifLeft = onError, ifRight = { it })

infix fun <A, B> Either<A, B>.`or else`(b: B) = this.fold(ifLeft = { b }, ifRight = { it })

fun <B> Either<EmailError, B>.get() =
    this.fold(ifLeft = { throw RuntimeException(it.message, it.cause) }, ifRight = { it })

