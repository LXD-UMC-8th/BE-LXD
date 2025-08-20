package org.lxdproject.lxd.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.validation.annotation.PageSizeValid;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageSizeValidator implements ConstraintValidator<PageSizeValid, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return value > 0 && value <= 100;
    }
}

