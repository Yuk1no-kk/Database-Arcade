package com.example.arcadesystem.controller;

import com.example.arcadesystem.dto.ApiResponse;
import com.example.arcadesystem.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions/recharge")
    public ApiResponse<Void> recharge(@RequestBody Map<String, Object> body) {
        int memberId = ((Number) body.get("memberId")).intValue();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        transactionService.recharge(memberId, amount);
        return ApiResponse.ok("Recharge successful", null);
    }

    @PostMapping("/transactions/consume")
    public ApiResponse<Void> consume(@RequestBody Map<String, Integer> body) {
        int memberId = body.get("memberId");
        int machineId = body.get("machineId");
        int tokens = body.get("tokens");
        transactionService.consume(memberId, machineId, tokens);
        return ApiResponse.ok("Consume successful", null);
    }

    @GetMapping("/transactions")
    public ApiResponse<Map<String, Object>> listTransactions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(transactionService.listTransactions(keyword, page, size));
    }
}
