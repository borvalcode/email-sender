package com.borvalcode.emailsender.infrastructure

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.borvalcode.emailsender.domain.EmailSender
import com.borvalcode.emailsender.domain.dto.Email
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Type.HTML
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Type.TEXT
import com.borvalcode.emailsender.domain.dto.EmailAddress
import com.borvalcode.emailsender.domain.dto.EmailError
import com.borvalcode.emailsender.domain.dto.EmailServer
import java.lang.IllegalArgumentException
import java.util.*
import java.util.stream.Collectors
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class DefaultEmailSender private constructor(emailServer: EmailServer) : EmailSender {
    private val session: Session

    constructor(host: String, port: Int) : this(EmailServer(host, port))

    init {
        val properties = Properties()
        properties["mail.smtp.host"] = emailServer.host
        properties["mail.smtp.port"] = emailServer.port
        session = Session.getInstance(properties)
    }

    companion object {
        @JvmStatic
        infix fun on(url: String) = try {
            DefaultEmailSender(EmailServer of url).right()
        } catch (ex: IllegalArgumentException) {
            EmailError(ex.message ?: "", ex).left()
        }

        @JvmStatic
        fun of(host: String, port: Int) = DefaultEmailSender(EmailServer(host, port))

    }

    override infix fun send(either: Either<EmailError, Email>): Either<EmailError, Unit> =
        try {
            either.map { email ->
                val message: Message = MimeMessage(session)
                message.setFrom(InternetAddress(email.from.value))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(tos(email.to)))
                message.subject = email.subject
                message.setContent(getContent(email))
                Transport.send(message)
            }
        } catch (e: MessagingException) {
            EmailError("Error sending email", e).left()
        }


    private fun tos(to: Set<EmailAddress>): String {
        return to.stream().map { obj: EmailAddress -> obj.value }.collect(Collectors.joining(","))
    }

    private fun getContent(email: Email): Multipart {
        val multipart: Multipart = MimeMultipart("alternative")

        addBodyParts(email.body, multipart)

        addAttachedFile(email.attachedFiles, multipart)

        return multipart
    }

    private fun addAttachedFile(attachedFiles: Set<String>?, multipart: Multipart) {
        attachedFiles?.map {
            val mimeBodyPart = MimeBodyPart()
            mimeBodyPart.attachFile(it)
            mimeBodyPart
        }?.forEach { mimeBodyPart ->
            multipart.addBodyPart(mimeBodyPart)
        }
    }

    private fun addBodyParts(emailBody: Email.Body, multipart: Multipart) {
        emailBody.parts.map { part ->
            val mimeBodyPart = MimeBodyPart()
            mimeBodyPart.setContent(part.content, getContentType(part.type))
            mimeBodyPart
        }.forEach { mimeBodyPart ->
            multipart.addBodyPart(mimeBodyPart)
        }
    }

    private fun getContentType(partType: Email.Body.Part.Type) =
        when (partType) {
            TEXT -> "text/plain; charset=UTF-8"
            HTML -> "text/html; charset=UTF-8"
        }

}

infix fun <A, B> Either<A, B>.`or else`(onError: (A) -> B) =
    this.fold(ifLeft = onError, ifRight = { it })

infix fun <A, B> Either<A, B>.`or else`(b: B) =
    this.fold(ifLeft = { b }, ifRight = { it })

fun <B> Either<EmailError, B>.get() = this.fold(ifLeft = { throw RuntimeException(it.message, it.cause) },
    ifRight = { it })