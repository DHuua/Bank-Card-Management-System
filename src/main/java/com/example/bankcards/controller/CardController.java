package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Cards", description = "Card management endpoints")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Create a new card")
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card by ID")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's cards with pagination")
    public ResponseEntity<Page<CardDTO>> getMyCards(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(cardService.getUserCards(pageable));
    }

    @GetMapping("/my/status/{status}")
    @Operation(summary = "Get current user's cards filtered by status")
    public ResponseEntity<Page<CardDTO>> getMyCardsByStatus(
            @PathVariable CardStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(cardService.getUserCardsByStatus(status, pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all cards with pagination (Admin only)")
    public ResponseEntity<Page<CardDTO>> getAllCards(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get cards by user ID")
    public ResponseEntity<List<CardDTO>> getCardsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @PutMapping("/{id}/block")
    @Operation(summary = "Block a card")
    public ResponseEntity<CardDTO> blockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.blockCard(id));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a card (Admin only)")
    public ResponseEntity<CardDTO> activateCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.activateCard(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a card (Admin only)")
    public ResponseEntity<Map<String, String>> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok(Map.of("message", "Card deleted successfully"));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get card balance")
    public ResponseEntity<Map<String, BigDecimal>> getCardBalance(@PathVariable Long id) {
        BigDecimal balance = cardService.getCardBalance(id);
        return ResponseEntity.ok(Map.of("balance", balance));
    }
}