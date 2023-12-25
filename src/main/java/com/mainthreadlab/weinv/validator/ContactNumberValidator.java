package com.mainthreadlab.weinv.validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


@Slf4j
public class ContactNumberValidator implements ConstraintValidator<ContactNumberConstraint, String> {

    @Override
    public void initialize(ContactNumberConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String contactField, ConstraintValidatorContext cxt) {

        // phoneNumber is optional
        if (StringUtils.isBlank(contactField)) {
//            throw new IllegalArgumentException("Phone number must not be null");
            return true;
        }

//        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
//        String phoneNumber = contactField.trim();
//        PhoneNumber phoneNumberParsed;
//        try {
//            phoneNumberParsed = phoneUtil.parse(phoneNumber, null);
//        } catch (NumberParseException e) {
//            log.error("Error when parsing phone number = {}", phoneNumber);
//            return false;
//        }
//        return phoneUtil.isValidNumber(phoneNumberParsed);
        return true;
    }

}
