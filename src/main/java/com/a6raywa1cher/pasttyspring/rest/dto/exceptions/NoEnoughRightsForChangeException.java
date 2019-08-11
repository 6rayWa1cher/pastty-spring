package com.a6raywa1cher.pasttyspring.rest.dto.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "No enough rights for requested change")
public class NoEnoughRightsForChangeException extends RuntimeException {
}
