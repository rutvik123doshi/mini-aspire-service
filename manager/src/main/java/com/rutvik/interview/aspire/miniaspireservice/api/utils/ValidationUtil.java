package com.rutvik.interview.aspire.miniaspireservice.api.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    public static final String MOBILE_NUMBER_REGEX = "^[0-9]{10}$";
    public boolean isValidPhoneNumber(String input) {
        // Compile the regex pattern into a Pattern object
        Pattern pattern = Pattern.compile(MOBILE_NUMBER_REGEX);

        Matcher matcher = pattern.matcher(input);

        // Return true if a match is found (i.e., it's a valid phone number)
        return matcher.matches();
    }
}
