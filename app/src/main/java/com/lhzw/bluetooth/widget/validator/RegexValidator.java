package com.lhzw.bluetooth.widget.validator;

import android.support.annotation.NonNull;


import com.lhzw.bluetooth.widget.ValidatedTextInputLayout;

import java.util.regex.Pattern;

/**
 * Validator to check if value satisfies given regular expression
 * @see BaseValidator
 */
public class RegexValidator extends BaseValidator {

    final String mRegex;

    /**
     * @param pRegex regular expression to check against
     * @param pErrorMessage error message to display if validation fails
     */
    public RegexValidator(@NonNull String pRegex, @NonNull String pErrorMessage) {
        super(pErrorMessage);
        mRegex = pRegex;
    }

    /**
     * Check if the associated {@link ValidatedTextInputLayout} has correct value.
     *
     * @param pText value associated with the input field
     * @return validity of the field
     */
    @Override
    public boolean isValid(String pText) {
        return Pattern.compile(mRegex, Pattern.CASE_INSENSITIVE).matcher(pText).find();
    }
}
