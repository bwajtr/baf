package com.wajtr.baf.user.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

const val EMAIL_PATTERN = ("^" + "[_A-Za-z0-9+]+(\\.[_A-Za-z0-9-]+)*" // local
        + "@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*" // domain
        + "\\." + "[a-zA-Z]{2,}" // tld
        + "$")

class EmailValidator : ConstraintValidator<ValidEmail, String> {

    private val pattern: Pattern = Pattern.compile(EMAIL_PATTERN)

    override fun initialize(constraintAnnotation: ValidEmail) {
    }

    override fun isValid(email: String, context: ConstraintValidatorContext?): Boolean {
        return isValid(email)
    }

    fun isValid(email: String): Boolean {
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

}