package com.faulttolerance.ecommerce.service;

import com.faulttolerance.ecommerce.client.ExchangeClient;
import com.faulttolerance.ecommerce.client.FidelityClient;
import com.faulttolerance.ecommerce.client.StoreClient;
import com.faulttolerance.ecommerce.dto.PurchaseRequest;
import com.faulttolerance.ecommerce.dto.PurchaseResponse;
import com.faulttolerance.ecommerce.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private StoreClient storeClient;

    @Mock
    private ExchangeClient exchangeClient;

    @Mock
    private FidelityClient fidelityClient;

    @InjectMocks
    private PurchaseService purchaseService;

    private PurchaseRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new PurchaseRequest();
        validRequest.setProductId("PROD-001");
        validRequest.setQuantity(1);
        validRequest.setCurrency("USD");
        validRequest.setUserId("USER-001");
    }

    @Test
    void processPurchase_Success() {
        // Arrange
        when(storeClient.checkProduct(any()))
            .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(100.00)));
        
        when(exchangeClient.getExchangeRate(any(), any()))
            .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(1.0)));
        
        when(storeClient.processSale(any()))
            .thenReturn(ResponseEntity.ok("SALE-001"));
        
        when(fidelityClient.processBonus(any()))
            .thenReturn(ResponseEntity.ok(10));

        // Act
        PurchaseResponse response = purchaseService.processPurchase(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SALE-001", response.getSaleId());
        assertEquals(10, response.getBonusPoints());
        assertEquals(BigDecimal.valueOf(100.00), response.getFinalPrice());
        
        verify(storeClient).checkProduct(validRequest.getProductId());
        verify(exchangeClient).getExchangeRate("USD", "BRL");
        verify(storeClient).processSale(any());
        verify(fidelityClient).processBonus(any());
    }

    @Test
    void processPurchase_ProductNotAvailable() {
        // Arrange
        when(storeClient.checkProduct(any()))
            .thenReturn(ResponseEntity.notFound().build());

        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> purchaseService.processPurchase(validRequest)
        );
        
        assertEquals("Product not available", exception.getMessage());
        verify(storeClient).checkProduct(validRequest.getProductId());
        verifyNoInteractions(exchangeClient, fidelityClient);
    }

    @Test
    void processPurchase_ExchangeServiceFailure() {
        // Arrange
        when(storeClient.checkProduct(any()))
            .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(100.00)));
        
        when(exchangeClient.getExchangeRate(any(), any()))
            .thenThrow(new RuntimeException("Exchange service unavailable"));

        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> purchaseService.processPurchase(validRequest)
        );
        
        assertEquals("Error processing exchange rate", exception.getMessage());
        verify(storeClient).checkProduct(validRequest.getProductId());
        verify(exchangeClient).getExchangeRate("USD", "BRL");
        verifyNoInteractions(fidelityClient);
    }

    @Test
    void processPurchase_SaleProcessingFailure() {
        // Arrange
        when(storeClient.checkProduct(any()))
            .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(100.00)));
        
        when(exchangeClient.getExchangeRate(any(), any()))
            .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(1.0)));
        
        when(storeClient.processSale(any()))
            .thenThrow(new RuntimeException("Error processing sale"));

        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> purchaseService.processPurchase(validRequest)
        );
        
        assertEquals("Error processing sale", exception.getMessage());
        verify(storeClient).checkProduct(validRequest.getProductId());
        verify(exchangeClient).getExchangeRate("USD", "BRL");
        verify(storeClient).processSale(any());
        verifyNoInteractions(fidelityClient);
    }

    @Test
    void processPurchase_FidelityServiceFailure_ShouldCompleteWithoutBonus() {
        // Arrange
        when(storeClient.checkProduct(any()))
            .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(100.00)));
        
        when(exchangeClient.getExchangeRate(any(), any()))
            .thenReturn(ResponseEntity.ok(BigDecimal.valueOf(1.0)));
        
        when(storeClient.processSale(any()))
            .thenReturn(ResponseEntity.ok("SALE-001"));
        
        when(fidelityClient.processBonus(any()))
            .thenThrow(new RuntimeException("Fidelity service unavailable"));

        // Act
        PurchaseResponse response = purchaseService.processPurchase(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SALE-001", response.getSaleId());
        assertEquals(0, response.getBonusPoints());
        assertEquals(BigDecimal.valueOf(100.00), response.getFinalPrice());
        
        verify(storeClient).checkProduct(validRequest.getProductId());
        verify(exchangeClient).getExchangeRate("USD", "BRL");
        verify(storeClient).processSale(any());
        verify(fidelityClient).processBonus(any());
    }

    @Test
    void processPurchase_InvalidRequest() {
        // Arrange
        PurchaseRequest invalidRequest = new PurchaseRequest();

        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> purchaseService.processPurchase(invalidRequest)
        );
        
        assertEquals("Invalid purchase request", exception.getMessage());
        verifyNoInteractions(storeClient, exchangeClient, fidelityClient);
    }
}
