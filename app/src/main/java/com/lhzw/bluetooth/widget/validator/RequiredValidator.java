package com.lhzw.bluetooth.widget.validator;


import com.lhzw.bluetooth.widget.ValidatedTextInputLayout;

/**
 * Validator to set input field as required.
 * @see BaseValidator
 */
public class RequiredValidator extends BaseValidator {

    /**
     * @param pErrorMessage error message to display if validation fails
     */
    public RequiredValidator(String pErrorMessage) {
        super(pErrorMessage);
    }

    /**
     * Check if the associated {@link ValidatedTextInputLayout} is empty or not.
     *
     * @param pText value associated with the input field
     * @return validity of the field
     */
    @Override
    public boolean isValid(String pText) {
        return !pText.isEmpty();
    }
}
