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

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleChatAlreadyExists (ChatAlreadyExistsException ex){
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage(), "CHAT_ALREADY_EXISTS"));
    }
    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatNotFound (ChatNotFoundException ex){
        log.error("Чат не найден : {}",ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage(), "CHAT_NOT_FOUND"));
    }
    @ExceptionHandler(LinkSubscribeException.class)
    public ResponseEntity<ErrorResponse> handleLinkSubscriptionException(LinkSubscribeException ex) {
        log.error("Ошибка в выполнеии  : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage(), "SUBSCRIPTION_ERROR"));
    }

    @ExceptionHandler(StockSubscribeException.class)
    public ResponseEntity<ErrorResponse> handleStockSubscribeException (StockSubscribeException ex){
        log.error("Ошибка в выполнении  : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage(),"ERROR SUBSCRIBE"));
    }
    @ExceptionHandler(ChatRegisterException.class)
    public ResponseEntity<ErrorResponse> handleChatRegisterException(ChatRegisterException ex) {
        log.error("Регистрация ошибки чата: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), "REGISTRATION_ERROR"));
    }
    @ExceptionHandler(ChatListException.class)
    public ResponseEntity<ErrorResponse> handleChatListException(ChatListException ex) {
        log.error("Ошибка получения списка чатов: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ex.getMessage(), "CHAT_LIST_ERROR"));
    }

    @ExceptionHandler(StockAlreadyExist.class)
    public ResponseEntity<ErrorResponse> handleStockExist(StockAlreadyExist ex){
        log.error("Ошибка в поиске акции : {} ", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(),"STOCK_NOT_FOUND"));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
    }

}
