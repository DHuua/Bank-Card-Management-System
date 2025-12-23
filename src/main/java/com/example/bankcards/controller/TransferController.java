package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.TransferStatus;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transfers", description = "Transfer management endpoints")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Create a transfer between own cards")
    public ResponseEntity<TransferDTO> createTransfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.createTransfer(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<TransferDTO> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's transfers with pagination")
    public ResponseEntity<Page<TransferDTO>> getMyTransfers(
            @PageableDefault(size = 10, sort = "transferDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transferService.getUserTransfers(pageable));
    }

    @GetMapping("/card/{cardId}")
    @Operation(summary = "Get transfers for a specific card")
    public ResponseEntity<Page<TransferDTO>> getTransfersByCardId(
            @PathVariable Long cardId,
            @PageableDefault(size = 10, sort = "transferDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transferService.getTransfersByCardId(cardId, pageable));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transfers within a date range")
    public ResponseEntity<List<TransferDTO>> getTransfersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(transferService.getTransfersByDateRange(startDate, endDate));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get transfers by status")
    public ResponseEntity<List<TransferDTO>> getTransfersByStatus(@PathVariable TransferStatus status) {
        return ResponseEntity.ok(transferService.getTransfersByStatus(status));
    }
}