package com.faulttolerance.ecommerce.controller;

import com.faulttolerance.ecommerce.model.PurchaseRequest;
import com.faulttolerance.ecommerce.model.PurchaseResponse;
import com.faulttolerance.ecommerce.service.PurchaseService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@Tag(name = "Purchase", description = "Purchase operations API")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/buy")
    @Operation(summary = "Process a purchase", description = "Processes a purchase request and returns the result asynchronously")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Purchase processed successfully",
            content = @Content(schema = @Schema(implementation = PurchaseResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error during purchase processing")
    })
    @Timed(value = "purchase.request", description = "Time taken to process purchase request")
    public CompletableFuture<ResponseEntity<PurchaseResponse>> purchase(
            @Parameter(description = "Purchase request details", required = true)
            @RequestBody PurchaseRequest request) {
        return purchaseService.processPurchase(request)
            .thenApply(response -> {
                if ("FAILED".equals(response.status())) {
                    return ResponseEntity.internalServerError().body(response);
                }
                return ResponseEntity.ok(response);
            });
    }
}
