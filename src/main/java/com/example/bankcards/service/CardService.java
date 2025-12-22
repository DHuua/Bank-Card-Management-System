package main.java.com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskingUtil cardMaskingUtil;

    @Transactional
    public CardDTO createCard(CardCreateRequest request) {
        User currentUser = userService.getCurrentUser();
        
        // Только админ может создавать карты для других пользователей
        User owner;
        if (request.getUserId() != null && !request.getUserId().equals(currentUser.getId())) {
            if (currentUser.getRole() != Role.ADMIN) {
                throw new UnauthorizedException("Only admins can create cards for other users");
            }
            owner = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        } else {
            owner = currentUser;
        }

        // Проверяем валидность номера карты
        if (!cardMaskingUtil.isValidCardNumber(request.getCardNumber())) {
            throw new BadRequestException("Invalid card number");
        }

        // Проверяем, не существует ли карта с таким номером
        String encryptedCardNumber = encryptionUtil.encrypt(request.getCardNumber());
        if (cardRepository.existsByCardNumber(encryptedCardNumber)) {
            throw new BadRequestException("Card with this number already exists");
        }

        // Проверяем дату истечения
        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Expiry date cannot be in the past");
        }

        // Создаем карту
        Card card = Card.builder()
                .cardNumber(encryptedCardNumber)
                .cardHolder(request.getCardHolder())
                .expiryDate(request.getExpiryDate())
                .cvv(request.getCvv() != null ? encryptionUtil.encrypt(request.getCvv()) : null)
                .status(CardStatus.ACTIVE)
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .owner(owner)
                .build();

        Card savedCard = cardRepository.save(card);
        return mapToDTO(savedCard);
    }

    public CardDTO getCardById(Long id) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));

        // Пользователь может видеть только свои карты, админ - все
        if (currentUser.getRole() != Role.ADMIN && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this card");
        }

        return mapToDTO(card);
    }

    public Page<CardDTO> getUserCards(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return cardRepository.findByOwner(currentUser, pageable).map(this::mapToDTO);
    }

    public Page<CardDTO> getUserCardsByStatus(CardStatus status, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return cardRepository.findByOwnerAndStatus(currentUser, status, pageable).map(this::mapToDTO);
    }

    public Page<CardDTO> getAllCards(Pageable pageable) {
        // Только для админов
        return cardRepository.findAll(pageable).map(this::mapToDTO);
    }

    public List<CardDTO> getCardsByUserId(Long userId) {
        User currentUser = userService.getCurrentUser();
        
        // Проверяем права доступа
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("You don't have access to these cards");
        }

        return cardRepository.findAllByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardDTO blockCard(Long id) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));

        // Пользователь может блокировать только свои карты
        if (currentUser.getRole() != Role.ADMIN && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this card");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new BadRequestException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card updatedCard = cardRepository.save(card);
        return mapToDTO(updatedCard);
    }

    @Transactional
    public CardDTO activateCard(Long id) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));

        // Только админ может активировать карту
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can activate cards");
        }

        if (card.isExpired()) {
            throw new BadRequestException("Cannot activate expired card");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updatedCard = cardRepository.save(card);
        return mapToDTO(updatedCard);
    }

    @Transactional
    public void deleteCard(Long id) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));

        // Только админ может удалять карты
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can delete cards");
        }

        cardRepository.delete(card);
    }

    public BigDecimal getCardBalance(Long id) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));

        // Пользователь может видеть баланс только своих карт
        if (currentUser.getRole() != Role.ADMIN && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this card");
        }

        return card.getBalance();
    }

    private CardDTO mapToDTO(Card card) {
        String decryptedCardNumber = encryptionUtil.decrypt(card.getCardNumber());
        String maskedCardNumber = cardMaskingUtil.maskCardNumber(decryptedCardNumber);

        return CardDTO.builder()
                .id(card.getId())
                .maskedCardNumber(maskedCardNumber)
                .cardHolder(card.getCardHolder())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .ownerUsername(card.getOwner().getUsername())
                .build();
    }

    // Внутренний метод для получения карты без DTO (для использования в TransferService)
    protected Card getCardEntity(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));
    }
}