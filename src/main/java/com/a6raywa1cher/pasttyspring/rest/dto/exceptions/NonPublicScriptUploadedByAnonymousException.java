package com.a6raywa1cher.pasttyspring.rest.dto.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Anonymous clients can't upload non-public scripts")
public class NonPublicScriptUploadedByAnonymousException extends RuntimeException {
}
