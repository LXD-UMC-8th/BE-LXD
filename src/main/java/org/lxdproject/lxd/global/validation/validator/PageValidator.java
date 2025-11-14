package org.lxdproject.lxd.global.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.lxdproject.lxd.global.validation.annotation.PageValid;

public class PageValidator implements ConstraintValidator<PageValid, Integer> {

    @Override
    public void initialize(PageValid constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || value > 0;
    }
}

