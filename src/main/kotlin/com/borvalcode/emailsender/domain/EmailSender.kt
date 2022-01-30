package com.borvalcode.emailsender.domain

import arrow.core.Either
import com.borvalcode.emailsender.domain.dto.Email
import com.borvalcode.emailsender.domain.dto.EmailError

interface EmailSender {
    infix fun send(either: Either<EmailError, Email>): Either<EmailError, Unit>
}

fun <A, B, C> Either<A, B>.handle( success: (B) -> C, error: (A) -> C) =
    this.fold(ifLeft = error, ifRight = success)


