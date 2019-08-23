package com.a6raywa1cher.pasttyspring.configs.validators.annotation;

import com.a6raywa1cher.pasttyspring.configs.validators.AuthConfigValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AuthConfigValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldDurationBigger {
	String message() default "Duration is smaller than another!";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String field();

	String biggerField();

	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@interface List {
		FieldDurationBigger[] value();
	}
}
