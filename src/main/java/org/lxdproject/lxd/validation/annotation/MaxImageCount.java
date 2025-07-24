package org.lxdproject.lxd.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.lxdproject.lxd.validation.validator.MaxImageCountValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxImageCountValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxImageCount {
    String message() default "이미지 개수 제한을 초과했습니다.";
    int max() default 5;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
