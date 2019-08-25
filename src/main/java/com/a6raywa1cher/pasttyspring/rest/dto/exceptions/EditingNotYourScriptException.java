package com.a6raywa1cher.pasttyspring.rest.dto.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Couldn't edit not your script")
public class EditingNotYourScriptException extends RuntimeException {
}
