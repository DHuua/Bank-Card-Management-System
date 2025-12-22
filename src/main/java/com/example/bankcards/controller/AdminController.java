package main.java.com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin", description = "Admin-only endpoints")
public class AdminController {

    private final UserService userService;
    private final CardService cardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        // Здесь можно добавить логику для сбора статистики
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "Admin dashboard");
        stats.put("info", "Statistics will be implemented here");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/cards")
    @Operation(summary = "Get all cards with pagination")
    public ResponseEntity<Page<CardDTO>> getAllCards(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Change user role")
    public ResponseEntity<UserDTO> changeUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        if (role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.ok(userService.promoteToAdmin(id));
        } else {
            return ResponseEntity.ok(userService.demoteToUser(id));
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user and all related data")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User and all related data deleted successfully"));
    }

    @DeleteMapping("/cards/{id}")
    @Operation(summary = "Delete card")
    public ResponseEntity<Map<String, String>> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok(Map.of("message", "Card deleted successfully"));
    }
}