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

    @PostMapping("/bonus/process")
    @Operation(
        summary = "Process bonus points",
        description = "Asynchronously process bonus points for a purchase with fault tolerance"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bonus points processed successfully",
            content = @Content(schema = @Schema(implementation = BonusPoints.class))),
        @ApiResponse(responseCode = "500", description = "Error processing bonus points")
    })
    @Timed(value = "bonus.process", description = "Time taken to process bonus points")
    public CompletableFuture<ResponseEntity<BonusPoints>> processBonus(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Order ID", required = true)
            @RequestParam Long orderId,
            @Parameter(description = "Purchase amount", required = true)
            @RequestParam Double purchaseAmount) {
        return fidelityService.processBonus(userId, orderId, purchaseAmount)
            .thenApply(ResponseEntity::ok)
            .exceptionally(throwable -> ResponseEntity.internalServerError().build());
    }

    @PostMapping("/bonus/confirm")
    @Operation(
        summary = "Confirm bonus points",
        description = "Confirm pending bonus points and add them to user's total points"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bonus points confirmed successfully",
            content = @Content(schema = @Schema(implementation = BonusPoints.class))),
        @ApiResponse(responseCode = "404", description = "No pending bonus points found"),
        @ApiResponse(responseCode = "500", description = "Error confirming bonus points")
    })
    @Timed(value = "bonus.confirm", description = "Time taken to confirm bonus points")
    public CompletableFuture<ResponseEntity<BonusPoints>> confirmBonus(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Order ID", required = true)
            @RequestParam Long orderId) {
        return fidelityService.confirmBonus(userId, orderId)
            .thenApply(ResponseEntity::ok)
            .exceptionally(throwable -> {
                if (throwable.getCause() instanceof IllegalArgumentException) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.internalServerError().build();
            });
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
