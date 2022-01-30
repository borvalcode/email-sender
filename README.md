# Email sender

### Declare the sender

```
    // Safe way of instantiating an EmailSender
    val defaultSender = EmailSender.of(host = "localhost", port = 25)

    // Non-safe way of instantiating an EmailSender: if you are sure that the url is [host]:[port], you can use method get()
    val sender = (EmailSender on "localhost:25").get()
    
    // Non-safe way of instantiating an EmailSender: if you can't be sure that the url is well-formed, you can declare a default object
    val orDefaultSender = EmailSender on inputUrl `or else` defaultSender

    // Non-safe way of instantiating an EmailSender: if you can't be sure that the url is well-formed, you can declare a routine if parsing fails
    val orThrowSender = EmailSender on inputUrl `or else` { throw RuntimeException(it.message, it.cause) }
```

### Create an email object

```
    // The simplest email
    val emailMessage = email from "sender@email.com" to "receiver@email.com" subject "Hello" body "Hello World"

    // Email with many receivers
    val emailWithReceivers = email from "sender@email.com" to setOf("receiver@email.com", "another@gmail.com") subject "Hello" body "Hello World"

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
            
```

### Send email

```
    sender send emailMessage

```

### Handle response 

```
    (sender send emailWithAttached)
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: $it") })

    (sender send emailWithComplexBody)
        .handle(onSuccess, onError)
        
    //...
        
private val onError: (EmailError) -> Unit = { println("Error sending email: $it") }

private val onSuccess: (Unit) -> Unit = { println("Mail sent!") }
```

### Single line integration

// Single line integration

```
    
    (EmailSender on "wrong_url")
        .flatMap { it send (email from "from@from.com" to "to@to.com" subject "subject" body "body") }
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: ${it.message}") })
    // Will print -> "Error sending email: Pattern must be [host]:[port]"

    (EmailSender on "localhost:25")
        .flatMap { it send (email from "from" to "to@to.com" subject "subject" body "body") }
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: ${it.message}") })
    // Will print -> "Error sending email: Invalid email address: from"

    (EmailSender on "localhost:25")
        .flatMap { it send (email from "from@from.com" to "to@to.com" subject "subject" body "body") }
        .handle(success = { println("Mail sent!") }, error = { println("Error sending email: ${it.message}") })
    // Will print -> "Error sending email: Error while sending" or "Mail sent!"
```
