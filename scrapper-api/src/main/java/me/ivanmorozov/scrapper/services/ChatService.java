package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.ChatServiceException;
import me.ivanmorozov.scrapper.repositories.TelegramChatRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    private final TelegramChatRepository chatRepository;

    public boolean registerChat(long chatId) {
        try {
            log.info("Чат {} успешно зарегистрирован", chatId);
            chatRepository.insertNewChat(chatId, LocalDateTime.now());
            return true;
        } catch (Exception e) {
            log.error("Ошибка регистрации чата {}: {}", chatId, e.getMessage());
            throw new ChatServiceException("Произошла ошибка в регистрации чата \n{error : " + e.getMessage() + "}",e);
        }
    }

    public Set<Long> getAllRegisteredChat() {
        try {
            Set<Long> chats = chatRepository.getAllChats();
            log.debug("Получено {} зарегистрированных чатов", chats.size());
            return chats;
        } catch (Exception e) {
            log.error("Ошибка получения списка чатов: {}", e.getMessage());
            throw new ChatServiceException("Ошибка получения списка чатов \n{error : " + e.getMessage() + "}", e);
        }
    }

    public boolean isChatExist(long chatId) {
        try {
            return chatRepository.existsById(chatId);
        } catch (Exception e) {
            log.error("Ошибка проверки существования чата {}: {}", chatId, e.getMessage());
            throw new ChatServiceException("Ошибка проверки чата \n{error : " + e.getMessage() + "}", e);
        }
    }

    public LocalDateTime getRegistrationTime(long chatId) {
        try {
            return Optional.ofNullable(chatRepository.getRegisterTime(chatId))
                    .orElseThrow(() -> {
                        log.debug("Чат {} не найден", chatId);
                        return new ChatServiceException("Чат не найден");
                    });
        } catch (ChatServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения времени регистрации чата {}: {}", chatId, e.getMessage());
            throw new RuntimeException("Ошибка сервиса при получении времени регистрации", e);
        }
    }

    public Flux<Long> getAllChatsWithRetry() {
        return Flux.defer(() -> Flux.fromIterable(getAllRegisteredChat()))
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .doOnError(e -> log.error("Окончательная ошибка получения чатов: {}", e.getMessage()));

    }

}
