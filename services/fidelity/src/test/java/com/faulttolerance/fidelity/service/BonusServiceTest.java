package com.faulttolerance.fidelity.service;

import com.faulttolerance.fidelity.dto.BonusRequest;
import com.faulttolerance.fidelity.entity.UserBonus;
import com.faulttolerance.fidelity.exception.ServiceException;
import com.faulttolerance.fidelity.repository.UserBonusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BonusServiceTest {

    @Mock
    private UserBonusRepository bonusRepository;

    @InjectMocks
    private BonusService bonusService;

    private ConcurrentHashMap<String, UserBonus> userBonusMap;

    private UserBonus validUserBonus;
    private BonusRequest validRequest;

    @BeforeEach
    void setUp() {
        userBonusMap = new ConcurrentHashMap<>();
        bonusService.setUserBonusMap(userBonusMap);

        validUserBonus = new UserBonus();
        validUserBonus.setUserId("USER-001");
        validUserBonus.setPoints(100);
        validUserBonus.setTotalPurchases(BigDecimal.valueOf(1000.00));

        validRequest = new BonusRequest();
        validRequest.setUserId("USER-001");
        validRequest.setPurchaseAmount(BigDecimal.valueOf(100.00));
        validRequest.setSaleId("SALE-001");
    }

    @Test
    void processBonus_NewUser_Success() {
        // Arrange
        when(bonusRepository.findByUserId(anyString()))
            .thenReturn(Optional.empty());

        // Act
        int points = bonusService.processBonus(validRequest);

        // Assert
        assertTrue(points > 0);
        verify(bonusRepository).findByUserId("USER-001");
    }

    @Test
    void processBonus_ExistingUser_Success() {
        // Arrange
        when(bonusRepository.findByUserId(anyString()))
            .thenReturn(Optional.of(validUserBonus));

        // Act
        int points = bonusService.processBonus(validRequest);

        // Assert
        assertTrue(points > 0);
        verify(bonusRepository).findByUserId("USER-001");
    }

    @Test
    void processBonus_InvalidRequest() {
        // Arrange
        BonusRequest invalidRequest = new BonusRequest();

        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> bonusService.processBonus(invalidRequest)
        );
        
        assertEquals("Invalid bonus request", exception.getMessage());
        verifyNoInteractions(bonusRepository);
    }

    @Test
    void processBonus_NegativeAmount() {
        // Arrange
        validRequest.setPurchaseAmount(BigDecimal.valueOf(-100.00));

        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> bonusService.processBonus(validRequest)
        );
        
        assertEquals("Invalid purchase amount", exception.getMessage());
        verifyNoInteractions(bonusRepository);
    }

    @Test
    void getUserPoints_Success() {
        // Arrange
        when(bonusRepository.findByUserId(anyString()))
            .thenReturn(Optional.of(validUserBonus));

        // Act
        int points = bonusService.getUserPoints("USER-001");

        // Assert
        assertEquals(validUserBonus.getPoints(), points);
        verify(bonusRepository).findByUserId("USER-001");
    }

    @Test
    void getUserPoints_UserNotFound() {
        // Arrange
        when(bonusRepository.findByUserId(anyString()))
            .thenReturn(Optional.empty());

        // Act
        int points = bonusService.getUserPoints("USER-001");

        // Assert
        assertEquals(0, points);
        verify(bonusRepository).findByUserId("USER-001");
    }

    @Test
    void calculateBonusPoints_RegularPurchase() {
        // Act
        int points = bonusService.calculateBonusPoints(
            BigDecimal.valueOf(100.00),
            BigDecimal.ZERO
        );

        // Assert
        assertEquals(10, points); // Assuming 10% bonus rate
    }

    @Test
    void calculateBonusPoints_LoyalCustomer() {
        // Act
        int points = bonusService.calculateBonusPoints(
            BigDecimal.valueOf(100.00),
            BigDecimal.valueOf(5000.00) // High total purchases
        );

        // Assert
        assertTrue(points > 10); // Should get loyalty bonus
    }

    @Test
    void updatePromotionMultiplier_Success() {
        // Act
        bonusService.setPromotionMultiplier(1.5);

        // Assert
        assertEquals(1.5, bonusService.getPromotionMultiplier());
    }

    @Test
    void updatePromotionMultiplier_Invalid() {
        // Act & Assert
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> bonusService.setPromotionMultiplier(0.0)
        );
        
        assertEquals("Invalid promotion multiplier", exception.getMessage());
    }
}
