package com.borvalcode.emailsender.infrastructure

import arrow.core.Either
import com.borvalcode.emailsender.domain.dto.Email.Body.Companion.of
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Html
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Text
import com.borvalcode.emailsender.domain.dto.Email.Companion.email
import com.borvalcode.emailsender.domain.handle
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.subethamail.wiser.Wiser
import java.io.File
import javax.mail.internet.MimeMultipart
import kotlin.test.Test

internal class DefaultEmailSenderTest {
    private val wiser: Wiser = Wiser()
    private val emailSender: DefaultEmailSender

    init {
        wiser.setPort(1234)
        wiser.setHostname("localhost")
        emailSender = DefaultEmailSender(wiser.server.hostName, wiser.server.port)
    }

    @BeforeEach
    fun setUp() {
        wiser.start()
    }

    @AfterEach
    fun tearDown() {
        wiser.stop()
    }

    @Test
    fun `create with correct pattern`() {
        invoking { (DefaultEmailSender on "localhost:25").get() } `should not throw` AnyException
    }

    @Test
    fun `send email successfully`() {


        (emailSender send (email from "me@me.com" to setOf("you@gmail.com", "him@him.com")
                subject "Hello" body "Hello World!"))
            .handle(success = { assert(true) }, error = { fail("Should not fail") })

        wiser.messages shouldHaveSize 2
        wiser.messages[0].envelopeSender `should be equal to` "me@me.com"
        wiser.messages[0].envelopeReceiver `should be equal to` "you@gmail.com"
        wiser.messages[0].mimeMessage.subject `should be equal to` "Hello"

        val mimeMultipart = wiser.messages[0].mimeMessage.content as MimeMultipart

        mimeMultipart.getBodyPart(0).contentType `should be equal to` TEXT_CONTENT_TYPE
        mimeMultipart.getBodyPart(0).content `should be equal to` "Hello World!"

    }

    @Test
    fun `create with incorrect pattern`() {
        invoking {
            (DefaultEmailSender on "localhost25").get()
        } `should throw` RuntimeException::class `with message` "Pattern must be [host]:[port], wrong localhost25"
    }

    @Test
    fun `if didn't parse could assign a default values one`() {
        invoking {
            DefaultEmailSender on "localhost25" `or else` DefaultEmailSender.of("localhost", 25)
        } `should not throw` AnyException
    }

    @Test
    fun `if didn't parse could assign a routine`() {
        invoking {
            DefaultEmailSender on "localhost25" `or else` { throw RuntimeException() }
        } `should throw` RuntimeException::class
    }

    @Test
    fun `send complex email successfully`() {

        val paths = setOf("File1.txt", "File2.txt")
        paths.createFiles()

        emailSender send (email from "me@me.com" to "you@gmail.com" subject "Hello"
                `attached files` paths
                body of(Text of "Hello", Html of "<html></html>", Text of "Good Bye"))

        wiser.messages shouldHaveSize 1
        wiser.messages[0].envelopeSender `should be equal to` "me@me.com"
        wiser.messages[0].envelopeReceiver `should be equal to` "you@gmail.com"
        wiser.messages[0].mimeMessage.subject `should be equal to` "Hello"

        val mimeMultipart = wiser.messages[0].mimeMessage.content as MimeMultipart

        mimeMultipart.getBodyPart(0).contentType `should be equal to` TEXT_CONTENT_TYPE
        mimeMultipart.getBodyPart(0).content `should be equal to` "Hello"

        mimeMultipart.getBodyPart(1).contentType `should be equal to` HTML_CONTENT_TYPE
        mimeMultipart.getBodyPart(1).content `should be equal to` "<html></html>"

        mimeMultipart.getBodyPart(2).contentType `should be equal to` TEXT_CONTENT_TYPE
        mimeMultipart.getBodyPart(2).content `should be equal to` "Good Bye"

        mimeMultipart.getBodyPart(3).fileName `should be equal to` "File1.txt"
        mimeMultipart.getBodyPart(4).fileName `should be equal to` "File2.txt"

        paths.deleteFiles()

    }

    @Test
    fun `throw error if server is not correct`() {

        val wrongSender = (DefaultEmailSender on "anyServer:12").get()
        val actual =
            wrongSender send (email from "me@me.com" to "to@to.com" subject "Hello" body (Text of "Hello world!"))

        (actual as Either.Left).value.message `should be equal to` "Error sending email"

    }

    @Test
    fun `throw error if email addresses are not correct`() {

        val actual = emailSender send (email from "me@me.com" to "not an email" subject "Hello" body "Hello world!")

        (actual as Either.Left).value.message `should be equal to` "Invalid email address: not an email"

    }

    companion object {
        const val TEXT_CONTENT_TYPE = "text/plain; charset=UTF-8"
        const val HTML_CONTENT_TYPE = "text/html; charset=UTF-8"
    }

    private fun Set<String>.createFiles() = this.forEach { File(it).createNewFile() }
    private fun Set<String>.deleteFiles() = this.forEach { File(it).delete() }
}