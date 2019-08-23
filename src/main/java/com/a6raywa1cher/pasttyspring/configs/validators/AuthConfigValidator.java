package com.a6raywa1cher.pasttyspring.configs.validators;

import com.a6raywa1cher.pasttyspring.configs.validators.annotation.FieldDurationBigger;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Duration;

public class AuthConfigValidator implements ConstraintValidator<FieldDurationBigger, Object> {
	private String field;

	private String biggerField;

	@Override
	public void initialize(FieldDurationBigger constraintAnnotation) {
		this.field = constraintAnnotation.field();
		this.biggerField = constraintAnnotation.biggerField();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
		Duration fieldValue = (Duration) beanWrapper.getPropertyValue(field);
		Duration biggerFieldValue = (Duration) beanWrapper.getPropertyValue(biggerField);
		if (fieldValue != null && biggerFieldValue != null) {
			return fieldValue.compareTo(biggerFieldValue) < 0;
		} else {
			return true;
		}
	}
}
