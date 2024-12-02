package com.faulttolerance.store.service;

import com.faulttolerance.store.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Test
    void getProduct_ExistingProduct_Success() {
        // Act
        Product product = productService.getProduct(1L);

        // Assert
        assertNotNull(product);
        assertEquals("Laptop", product.name());
        assertEquals(new BigDecimal("999.99"), product.price());
        assertEquals("USD", product.currency());
        assertEquals(10, product.stock());
    }

    @Test
    void getProduct_NonExistingProduct_ReturnsNull() {
        // Act
        Product product = productService.getProduct(999L);

        // Assert
        assertNull(product);
    }

    @Test
    void updateStock_ExistingProduct_Success() {
        // Arrange
        Long productId = 1L;
        Product beforeUpdate = productService.getProduct(productId);
        int quantityToReduce = 2;
        
        // Act
        productService.updateStock(productId, quantityToReduce);
        
        // Assert
        Product afterUpdate = productService.getProduct(productId);
        assertNotNull(afterUpdate);
        assertEquals(beforeUpdate.stock() - quantityToReduce, afterUpdate.stock());
    }

    @Test
    void updateStock_NonExistingProduct_NoEffect() {
        // Act
        productService.updateStock(999L, 1);
        
        // Assert
        assertNull(productService.getProduct(999L));
    }
}
