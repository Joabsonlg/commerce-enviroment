package com.faulttolerance.fidelity.controller;

import com.faulttolerance.fidelity.model.BonusPoints;
import com.faulttolerance.fidelity.model.UserPoints;
import com.faulttolerance.fidelity.service.FidelityService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@Tag(name = "Fidelity", description = "Bonus points management API")
public class FidelityController {
    private final FidelityService fidelityService;

    public FidelityController(FidelityService fidelityService) {
        this.fidelityService = fidelityService;
    }

    @GetMapping("/bonus")
    @Operation(
            summary = "Process or Confirm bonus points",
            description = "If purchaseAmount is provided and > 0, process bonus points. Otherwise, confirm pending bonus points."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation performed successfully",
                    content = @Content(schema = @Schema(implementation = BonusPoints.class))),
            @ApiResponse(responseCode = "404", description = "No pending bonus found when confirming"),
            @ApiResponse(responseCode = "500", description = "Error processing or confirming bonus points")
    })
    @Timed(value = "bonus.operation", description = "Time taken to process or confirm bonus points")
    public CompletableFuture<? extends ResponseEntity<?>> handleBonusOperation(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Order ID", required = true)
            @RequestParam Long orderId,
            @Parameter(description = "Purchase amount (optional). If provided and >0, process bonus points. Otherwise, confirm bonus points.")
            @RequestParam(required = false) Double purchaseAmount) {

        if (purchaseAmount != null && purchaseAmount > 0) {
            return fidelityService.processBonus(userId, orderId, purchaseAmount)
                    .thenApply(bonusPoints -> {
                        if (bonusPoints == null) {
                            return ResponseEntity.ok().build();
                        }
                        return ResponseEntity.ok(bonusPoints);
                    })
                    .exceptionally(throwable -> ResponseEntity.internalServerError().build());
        } else {
            return fidelityService.confirmBonus(userId, orderId)
                    .thenApply(ResponseEntity::ok)
                    .exceptionally(throwable -> {
                        if (throwable.getCause() instanceof IllegalArgumentException) {
                            return ResponseEntity.notFound().build();
                        }
                        return ResponseEntity.internalServerError().build();
                    });
        }
    }

    @GetMapping("/points/{userId}")
    @Operation(
            summary = "Get user points",
            description = "Get user's total and pending bonus points"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User points retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserPoints.class)))
    })
    @Timed(value = "points.get", description = "Time taken to get user points")
    public ResponseEntity<UserPoints> getUserPoints(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(fidelityService.getUserPoints(userId));
    }
}
