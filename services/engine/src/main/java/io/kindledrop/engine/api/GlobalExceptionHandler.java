package io.kindledrop.engine.api;

import java.net.SocketTimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<SendResponse> handleIllegalArgument(IllegalArgumentException ex) {
        SendResponse body = new SendResponse(false, null, null, 0L, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({MailAuthenticationException.class, MailSendException.class})
    public ResponseEntity<SendResponse> handleMailExceptions(Exception ex) {
        String msg = "SMTP failed: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        SendResponse body = new SendResponse(false, null, null, 0L, msg);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<SendResponse> handleSocketTimeout(SocketTimeoutException ex) {
        SendResponse body = new SendResponse(false, null, null, 0L, "SMTP timeout");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SendResponse> handleFallback(Exception ex) {
        SendResponse body = new SendResponse(false, null, null, 0L, "Unexpected error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
