package com.a6raywa1cher.pasttyspring.rest.dto.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Name already taken")
public class ScriptWithProvidedNameExistsException extends RuntimeException {
}
