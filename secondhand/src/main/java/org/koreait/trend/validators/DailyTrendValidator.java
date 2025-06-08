package org.koreait.trend.validators;

import lombok.RequiredArgsConstructor;
import org.koreait.global.validators.UrlValidator;
import org.koreait.member.controllers.RequestJoin;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Lazy
@Component
@RequiredArgsConstructor
public class DailyTrendValidator implements Validator, UrlValidator {
    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }

        /**
         * 이메일 형식
         */
        RequestJoin form = (RequestJoin) target;
        String password = form.getPassword();
        String confirmPassword = form.getConfirmPassword();
        String mobile = form.getMobile();

        // 1. 이메일 중복 여부
        if (repository.existsByEmail(form.getEmail())) {
            errors.rejectValue("email", "Duplicated");
        }
    }
}
