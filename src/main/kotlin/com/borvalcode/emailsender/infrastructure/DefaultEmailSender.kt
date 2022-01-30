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
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

typealias EmailSender = DefaultEmailSender

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
            EmailError("Error while sending", e).left()
        }


    private fun tos(tos: Set<EmailAddress>): String {
        return tos.joinToString(",") { it.value }
    }

    private fun getContent(email: Email) =
        MimeMultipart("alternative").let {
            addBodyParts(email.body, it)
            addAttachedFile(email.attachedFiles, it)
            it
        }

    private fun addAttachedFile(attachedFiles: Set<String>?, multipart: Multipart) {
        attachedFiles?.map {
            val mimeBodyPart = MimeBodyPart()
            mimeBodyPart.attachFile(it)
            mimeBodyPart
        }?.forEach { multipart.addBodyPart(it) }
    }

    private fun addBodyParts(emailBody: Email.Body, multipart: Multipart) {
        emailBody.parts.map { part ->
            val mimeBodyPart = MimeBodyPart()
            mimeBodyPart.setContent(part.content, getContentType(part.type))
            mimeBodyPart
        }.forEach { multipart.addBodyPart(it) }
    }

    private fun getContentType(partType: Email.Body.Part.Type) =
        when (partType) {
            TEXT -> "text/plain; charset=UTF-8"
            HTML -> "text/html; charset=UTF-8"
        }

}

