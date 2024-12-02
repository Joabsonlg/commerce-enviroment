package com.faulttolerance.exchange.service;

import com.faulttolerance.exchange.client.ExternalExchangeClient;
import com.faulttolerance.exchange.dto.ExchangeRate;
import com.faulttolerance.exchange.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private ExternalExchangeClient exchangeClient;

    @Mock
    private RedisTemplate<String, ExchangeRate> redisTemplate;

    @Mock
    private ValueOperations<String, ExchangeRate> valueOperations;

    @InjectMocks
    private ExchangeService exchangeService;

    private ExchangeRate validRate;

    @BeforeEach
    void setUp() {
        validRate = new ExchangeRate();
        validRate.setFromCurrency("USD");
        validRate.setToCurrency("BRL");
        validRate.setRate(BigDecimal.valueOf(5.0));
        validRate.setTimestamp(System.currentTimeMillis());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getExchangeRate_FromCache_Success() {
        // Arrange
        when(valueOperations.get(anyString()))
            .thenReturn(validRate);

        // Act
        ExchangeRate result = exchangeService.getExchangeRate("USD", "BRL");

        // Assert
        assertNotNull(result);
        assertEquals(validRate.getRate(), result.getRate());
        assertEquals(validRate.getFromCurrency(), result.getFromCurrency());
        assertEquals(validRate.getToCurrency(), result.getToCurrency());
        
        verify(valueOperations).get("USD:BRL");
        verifyNoInteractions(exchangeClient);
    }

    @Test
    void getExchangeRate_FromExternalService_Success() {
        // Arrange
        when(valueOperations.get(anyString()))
            .thenReturn(null);
        when(exchangeClient.fetchExchangeRate(anyString(), anyString()))
            .thenReturn(validRate);

        // Act
        ExchangeRate result = exchangeService.getExchangeRate("USD", "BRL");

        // Assert
        assertNotNull(result);
        assertEquals(validRate.getRate(), result.getRate());
        assertEquals(validRate.getFromCurrency(), result.getFromCurrency());
        assertEquals(validRate.getToCurrency(), result.getToCurrency());
        
        verify(valueOperations).get("USD:BRL");
        verify(exchangeClient).fetchExchangeRate("USD", "BRL");
        verify(valueOperations).set(eq("USD:BRL"), any(ExchangeRate.class), any(Duration.class));
    }

    @Test
    void getExchangeRate_ExternalServiceFailure_WithCachedFallback() {
        // Arrange
        ExchangeRate oldRate = new ExchangeRate();
        oldRate.setFromCurrency("USD");
        oldRate.setToCurrency("BRL");
        oldRate.setRate(BigDecimal.valueOf(4.9));
        oldRate.setTimestamp(System.currentTimeMillis() - 3600000); // 1 hour old

        when(valueOperations.get(anyString()))
            .thenReturn(oldRate);
        when(exchangeClient.fetchExchangeRate(anyString(), anyString()))
            .thenThrow(new RuntimeException("External service unavailable"));

        // Act
        ExchangeRate result = exchangeService.getExchangeRate("USD", "BRL");

        // Assert
        assertNotNull(result);
        assertEquals(oldRate.getRate(), result.getRate());
        assertTrue(result.isStale());
        
        verify(valueOperations).get("USD:BRL");
        verify(exchangeClient).fetchExchangeRate("USD", "BRL");
    }

    @Test
    void getExchangeRate_CompleteFailure() {
        // Arrange
        when(valueOperations.get(anyString()))
            .thenReturn(null);
        when(exchangeClient.fetchExchangeRate(anyString(), anyString()))
            .thenThrow(new RuntimeException("External service unavailable"));

        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> exchangeService.getExchangeRate("USD", "BRL")
        );
        
        assertEquals("Failed to get exchange rate", exception.getMessage());
        verify(valueOperations).get("USD:BRL");
        verify(exchangeClient).fetchExchangeRate("USD", "BRL");
    }

    @Test
    void getExchangeRate_InvalidCurrency() {
        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> exchangeService.getExchangeRate("", "BRL")
        );
        
        assertEquals("Invalid currency code", exception.getMessage());
        verifyNoInteractions(valueOperations, exchangeClient);
    }

    @Test
    void getExchangeRate_SameCurrency() {
        // Act
        ExchangeRate result = exchangeService.getExchangeRate("USD", "USD");

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ONE, result.getRate());
        assertEquals("USD", result.getFromCurrency());
        assertEquals("USD", result.getToCurrency());
        assertFalse(result.isStale());
        
        verifyNoInteractions(valueOperations, exchangeClient);
    }

    @Test
    void refreshExchangeRate_Success() {
        // Arrange
        when(exchangeClient.fetchExchangeRate(anyString(), anyString()))
            .thenReturn(validRate);

        // Act
        ExchangeRate result = exchangeService.refreshExchangeRate("USD", "BRL");

        // Assert
        assertNotNull(result);
        assertEquals(validRate.getRate(), result.getRate());
        assertFalse(result.isStale());
        
        verify(exchangeClient).fetchExchangeRate("USD", "BRL");
        verify(valueOperations).set(eq("USD:BRL"), any(ExchangeRate.class), any(Duration.class));
    }

    @Test
    void refreshExchangeRate_Failure() {
        // Arrange
        when(exchangeClient.fetchExchangeRate(anyString(), anyString()))
            .thenThrow(new RuntimeException("External service unavailable"));

        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> exchangeService.refreshExchangeRate("USD", "BRL")
        );
        
        assertEquals("Failed to refresh exchange rate", exception.getMessage());
        verify(exchangeClient).fetchExchangeRate("USD", "BRL");
        verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    void getExchangeRate_SameCurrency_ReturnsOne() {
        // Act
        ExchangeRate rate = exchangeService.getExchangeRate("USD", "USD");

        // Assert
        assertNotNull(rate);
        assertEquals("USD", rate.getFromCurrency());
        assertEquals("USD", rate.getToCurrency());
        assertEquals(BigDecimal.ONE, rate.getRate());
        assertNotNull(rate.getTimestamp());
    }

    @Test
    void getExchangeRate_USDBRL_Success() {
        // Act
        ExchangeRate rate = exchangeService.getExchangeRate("USD", "BRL");

        // Assert
        assertNotNull(rate);
        assertEquals("USD", rate.getFromCurrency());
        assertEquals("BRL", rate.getToCurrency());
        assertTrue(rate.getRate().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(rate.getTimestamp());
    }

    @Test
    void getExchangeRate_InvalidCurrency_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> exchangeService.getExchangeRate("USD", "INVALID")
        );
        
        assertEquals("Exchange rate not found", exception.getMessage());
    }

    @Test
    void getExchangeRate_MultipleCalls_ReturnsSameRate() {
        // Act
        ExchangeRate rate1 = exchangeService.getExchangeRate("USD", "BRL");
        ExchangeRate rate2 = exchangeService.getExchangeRate("USD", "BRL");

        // Assert
        assertEquals(rate1.getRate(), rate2.getRate());
    }
}
