package me.ivanmorozov.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    public record ErrorResponse(String msg, String errorCode){}


    @ExceptionHandler(LinkServiceException.class)
    public ResponseEntity<ErrorResponse> handleLinkSubscriptionException(LinkServiceException ex) {
        log.error("Ошибка в выполнеии  : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage(), "ERROR_ON_LINK_SERVICE"));
    }

    @ExceptionHandler(StockServiceException.class)
    public ResponseEntity<ErrorResponse> handleLinkSubscriptionException(StockServiceException ex) {
        log.error("Ошибка в выполнеии  : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage(), "ERROR_ON_STOCK_SERVICE"));
    }
    @ExceptionHandler(ChatServiceException.class)
    public ResponseEntity<ErrorResponse> handleLinkSubscriptionException(ChatServiceException ex) {
        log.error("Ошибка в выполнеии  : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage(), "ERROR_ON_STOCK_SERVICE"));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
    }

}
