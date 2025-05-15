package me.ivanmorozov.scrapper.repositories.old;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
@RequiredArgsConstructor
public class TgChatRepository {
    private final  UserSubsLinkRepository subsLinkRepository;
   public final ConcurrentHashMap<Long, LocalDateTime> chats = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        chats.clear();
        System.err.println(chats);
    }
    public void add(Long id){
        chats.put(id, LocalDateTime.now());
        System.err.println(chats);
        log.info("NEW ADD / "+"Добавление нового чата " + id);
    }

    public Set<Long> getAllChats (){
      return new HashSet<>(chats.keySet());
    }

    public void addSubscriptionLink(long chatId, String link){
        subsLinkRepository.addSubscription(chatId,link);
    }

    public boolean exist(Long id){
       return chats.containsKey(id);
    }

    public void delete(Long id){
        chats.remove(id);
        log.info("DELETE / "+"Удаление нового чата " + id);
    }

    public LocalDateTime getRegisterTime(Long id){
        return chats.get(id);
    }


    @Override
    public String toString() {
        return "TgChatRepository{" +
                "chats=" + chats +
                '}';
    }
}
