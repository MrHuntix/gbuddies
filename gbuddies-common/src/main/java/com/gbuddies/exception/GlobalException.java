package com.gbuddies.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//TODO implement a common exception handler
@ControllerAdvice
public class GlobalException extends ResponseEntityExceptionHandler {
}
