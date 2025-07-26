package org.lxdproject.lxd.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.validation.annotation.MaxImageCount;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class MaxImageCountValidator implements ConstraintValidator<MaxImageCount, String> {

    private int max;

    @Override
    public void initialize(MaxImageCount constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null 값은 @NotBlank로 처리함
        }

        int count = countImgTags(value);
        boolean isValid = count <= max;

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(ErrorStatus.TOO_MANY_IMAGES.toString())
                    .addConstraintViolation();
        }

        return isValid;
    }

    private int countImgTags(String html) {
        Pattern pattern = Pattern.compile("<img[^>]+src=[\"']?([^\"'>]+)[\"']?");
        Matcher matcher = pattern.matcher(html);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }
}
