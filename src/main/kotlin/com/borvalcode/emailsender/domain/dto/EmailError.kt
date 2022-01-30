package com.borvalcode.emailsender.domain.dto

data class EmailError (val message: String, var cause: Throwable? = null)