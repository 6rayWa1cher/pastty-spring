package com.a6raywa1cher.pasttyspring.rest.dto.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Script isn't executable")
public class NotExecScriptException extends RuntimeException {
}
