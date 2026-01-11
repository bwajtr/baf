package com.wajtr.baf.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator
        implements ConstraintValidator<ValidEmail, String> {

    private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private static final String EMAIL_PATTERN = "^" + "[_A-Za-z0-9+]+(\\.[_A-Za-z0-9-]+)*" // local
            + "@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*" // domain
            + "\\." + "[a-zA-Z]{2,}" // tld
            + "$";

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return isValid(email);
    }

    public boolean isValid(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}