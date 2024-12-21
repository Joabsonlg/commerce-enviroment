package com.faulttolerance.fidelity.controller;

import com.faulttolerance.fidelity.service.FidelityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class FidelityController {

    private final FidelityService fidelityService;

    public FidelityController(FidelityService fidelityService) {
        this.fidelityService = fidelityService;
    }

    @PostMapping("/bonus")
    public ResponseEntity<Void> handleBonusOperation(
            @RequestParam Long user,
            @RequestParam Integer bonus) {
        try {
            fidelityService.handleBonus(user, bonus);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }
}