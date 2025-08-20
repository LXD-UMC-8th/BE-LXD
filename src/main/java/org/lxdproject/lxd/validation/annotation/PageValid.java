package org.lxdproject.lxd.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.lxdproject.lxd.validation.validator.PageValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PageValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface PageValid {
    String message() default "요청한 페이지는 1 이상이어야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

