package com.groo83.point.common.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private ValidEnum annotation;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = false;
        Object[] enumValues = this.annotation.target().getEnumConstants();
        if (enumValues != null) {
            for (Object enumVal : enumValues) {
                if (enumVal instanceof Enum enumValue) {
                    if (value.equals(enumValue.name())
                            || (this.annotation.ignoreCase() && value.equalsIgnoreCase(enumValue.name()))) {
                        result = true;
                        break;
                    }
                } else {
                    if (value.equals(enumVal.toString())
                            || (this.annotation.ignoreCase() && value.equalsIgnoreCase(enumVal.toString()))) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }
}