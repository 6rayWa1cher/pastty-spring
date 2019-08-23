package com.a6raywa1cher.pasttyspring.rest;

public final class ControllerValidations {
	public static final String USERNAME_REGEX = "[A-Za-z0-9_-]{2,20}";

	public static final String SCRIPT_NAME_REGEX = "[a-z0-9_-]{3,25}";

	// https://www.regexpal.com/105777
	public static final String JWT_REGEX = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

	public static final int PASSWORD_LENGTH = 255;

	public static final int SCRIPT_NAME_LENGTH = 35;

	public static final int SCRIPT_TITLE_LENGTH = 150;

	public static final int SCRIPT_DESCRIPTION_LENGTH = 5000;

	public static final int SCRIPT_DIALECT_LENGTH = 20;
}
