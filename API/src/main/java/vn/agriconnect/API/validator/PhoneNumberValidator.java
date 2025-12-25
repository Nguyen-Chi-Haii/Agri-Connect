package vn.agriconnect.API.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private static final String PHONE_PATTERN = "^(\\+84|0)[3|5|7|8|9][0-9]{8}$";

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isEmpty()) {
            return true; // Let @NotBlank handle null/empty
        }
        return phone.matches(PHONE_PATTERN);
    }
}
