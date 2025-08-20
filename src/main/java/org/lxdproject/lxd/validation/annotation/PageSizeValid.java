package org.lxdproject.lxd.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.lxdproject.lxd.validation.validator.PageSizeValidator;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PageSizeValidator.class)
@Documented
public @interface PageSizeValid {
    String message() default "요청한 페이지 크기는 1~100 사이여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

