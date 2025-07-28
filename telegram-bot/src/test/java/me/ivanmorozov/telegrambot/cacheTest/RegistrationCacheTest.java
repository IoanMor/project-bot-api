package me.ivanmorozov.telegrambot.cacheTest;

import me.ivanmorozov.telegrambot.cache.RegistrationCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationCacheTest {
    @InjectMocks
    private RegistrationCache registrationCache;

    @Test
    public void setRegistered_dataShouldAddInCache(){
        registrationCache.setRegistered(1L,true);
        Boolean isReg = registrationCache.isRegistered(1L);
        assertTrue(isReg);
    }

    @Test
    void getUnregisteredChatId_shouldReturnNull() {
        Boolean isRegistered = registrationCache.isRegistered(999L);
        assertNull(isRegistered);
    }

}
