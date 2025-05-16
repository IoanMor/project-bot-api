package me.ivanmorozov.scrapper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ivanmorozov.common.exception.ChatAlreadyExistsException;
import me.ivanmorozov.common.exception.ChatListException;
import me.ivanmorozov.common.exception.ChatNotFoundException;
import me.ivanmorozov.common.exception.ChatRegisterException;
import me.ivanmorozov.scrapper.repositories.TelegramChatRepository;
import org.springframework.stereotype.Service;

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
            throw new ChatRegisterException("Произошла ошибка в регистрации чата \n{error : " + e.getMessage() + "}");
        }
    }

    public Set<Long> getAllRegisteredChat() {
        try {
            Set<Long> chats = chatRepository.getAllChats();
            log.debug("Получено {} зарегистрированных чатов", chats.size());
            return chats;
        } catch (Exception e) {
            log.error("Ошибка получения списка чатов: {}", e.getMessage());
            throw new ChatListException("Ошибка получения списка чатов \n{error : " + e.getMessage() + "}");
        }
    }

    public boolean isChatExist(long chatId) {
        try {
            return chatRepository.existsById(chatId);
        } catch (Exception e) {
            log.error("Ошибка проверки существования чата {}: {}", chatId, e.getMessage());
            throw new ChatAlreadyExistsException("Ошибка проверки чата \n{error : " + e.getMessage() + "}");
        }
    }

    public LocalDateTime getRegistrationTime(long chatId) {
        try {
            return Optional.ofNullable(chatRepository.getRegisterTime(chatId))
                    .orElseThrow(() -> {
                        log.debug("Чат {} не найден", chatId);
                        return new ChatNotFoundException(chatId);
                    });
        } catch (ChatNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка получения времени регистрации чата {}: {}", chatId, e.getMessage());
            throw new RuntimeException("Ошибка сервиса при получении времени регистрации", e);
        }
    }

}
