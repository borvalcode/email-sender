package com.borvalcode.emailsender.domain.dto

import arrow.core.left
import arrow.core.right
import com.borvalcode.emailsender.domain.dto.Email.Body.Part
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Type.HTML
import com.borvalcode.emailsender.domain.dto.Email.Body.Part.Type.TEXT


class Email private constructor(
    internal val from: EmailAddress,
    internal val to: Set<EmailAddress>,
    val subject: String,
    var attachedFiles: Set<String>? = null,
    val body: Body
) {

    companion object {
        val email get() = EmptyEmail()
    }


    data class Body(val parts: List<Part>) {

        sealed class Part(val type: Type, val content: String) {

            enum class Type { TEXT, HTML}


            class Text(content: String) : Part(TEXT, content) {
                companion object {
                    infix fun of(content: String) = Text(content)
                }
            }

            class Html(content: String) : Part(HTML, content){
                companion object {
                    infix fun of(content: String) = Html(content)
                }
            }
        }
    }

    class EmptyEmail {
        infix fun from(from: String) = FromEmail(this, from)
    }

    class FromEmail(private val previous: EmptyEmail,  val from: String) {
        infix fun to(to: String) = FromAndToEmail(this, setOf(to))
        infix fun to(to: Set<String>) = FromAndToEmail(this, to)
    }

    class FromAndToEmail(val previous: FromEmail, val to: Set<String>) {
        infix fun subject(subject: String) = FromAndToAndSubjectEmail(this, subject, null)
    }

    class FromAndToAndSubjectEmail(private val previous: FromAndToEmail, private val subject: String, private var attachedFiles: Set<String>?) {
        infix fun `attached files`(attachedFiles: Set<String>) = FromAndToAndSubjectEmail(previous,subject, attachedFiles)
        infix fun body(part: Part) = body(listOf(part))
        infix fun body(parts: List<Part>) = body(Body(parts))
        infix fun body(body: String) = body(Part.Text of body)
        infix fun body(body: Body) = try {
            Email(EmailAddress of previous.previous.from, previous.to.map { EmailAddress of it }.toSet(), subject, attachedFiles, body).right()
        } catch (ex: IllegalArgumentException) {
            EmailError(ex.message ?: "", ex).left()
        }
    }

}
