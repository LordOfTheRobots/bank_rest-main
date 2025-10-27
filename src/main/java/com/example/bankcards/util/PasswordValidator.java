package com.example.bankcards.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {
    private static final Logger logger = LoggerFactory.getLogger(PasswordValidator.class);

    @Value("${app.security.password.min-length}")
    private Integer minPasswordLength;

    @Value("${app.security.password.require-lowercase}")
    private Boolean lowercaseNeeded;

    @Value("${app.security.password.require-uppercase}")
    private Boolean uppercaseNeeded;

    @Value("${app.security.password.require-numbers}")
    private Boolean numbersNeeded;

    @Value("${app.security.password.require-special-chars}")
    private Boolean specialCharsNeeded;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN =
            Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    public boolean isValid(String password) {
        logger.debug("Validating password with length: {}", password == null ? 0 : password.length());
        Boolean valid = true;
        if (
                (password == null || password.trim().isEmpty())
                        || (password.length() < minPasswordLength)
                        || (uppercaseNeeded && !UPPERCASE_PATTERN.matcher(password).matches())
                        || (lowercaseNeeded && !LOWERCASE_PATTERN.matcher(password).matches())
                        || (numbersNeeded && !NUMBER_PATTERN.matcher(password).matches())
                        || (specialCharsNeeded && !SPECIAL_CHAR_PATTERN.matcher(password).matches())
        ) {
            valid = false;
        }
        logger.debug("Password validation result: {}", valid);
        return valid;
    }

    public List<String> validateWithDetails(String password) {
        logger.debug("Validating password with detailed rules");
        List<String> errors = new ArrayList<>();

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Password is empty");
            errors.add("Password cannot be empty");
            return errors;
        }

        if (password.length() < minPasswordLength) {
            logger.debug("Password length {} is less than required {}", password.length(), minPasswordLength);
            errors.add("Password must be at least " + minPasswordLength + " characters long");
        }

        if (uppercaseNeeded && !UPPERCASE_PATTERN.matcher(password).matches()) {
            logger.debug("Password missing uppercase letter");
            errors.add("Password must contain at least one uppercase letter (A-Z)");
        }

        if (lowercaseNeeded && !LOWERCASE_PATTERN.matcher(password).matches()) {
            logger.debug("Password missing lowercase letter");
            errors.add("Password must contain at least one lowercase letter (a-z)");
        }

        if (numbersNeeded && !NUMBER_PATTERN.matcher(password).matches()) {
            logger.debug("Password missing number");
            errors.add("Password must contain at least one number (0-9)");
        }

        if (specialCharsNeeded && !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            logger.debug("Password missing special character");
            errors.add("Password must contain at least one special character (!@#$%^&*() etc.)");
        }

        logger.debug("Password validation found {} errors", errors.size());
        return errors;
    }
}