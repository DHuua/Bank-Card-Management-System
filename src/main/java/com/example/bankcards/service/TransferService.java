package main.java.com.example.bankcards.service;

import com.example.bankcards.dto.TransferDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.TransferStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardService cardService;
    private final UserService userService;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskingUtil cardMaskingUtil;

    @Transactional
    public TransferDTO createTransfer(TransferRequest request) {
        User currentUser = userService.getCurrentUser();

        // Получаем карты
        Card fromCard = cardService.getCardEntity(request.getFromCardId());
        Card toCard = cardService.getCardEntity(request.getToCardId());

        // Проверяем, что обе карты принадлежат текущему пользователю
        if (!fromCard.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Source card does not belong to you");
        }

        if (!toCard.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only transfer between your own cards");
        }

        // Проверяем, что карты разные
        if (fromCard.getId().equals(toCard.getId())) {
            throw new BadRequestException("Cannot transfer to the same card");
        }

        // Проверяем статусы карт
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new BadRequestException("Source card is not active");
        }

        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new BadRequestException("Destination card is not active");
        }

        // Проверяем, не истек ли срок карт
        if (fromCard.isExpired()) {
            throw new BadRequestException("Source card has expired");
        }

        if (toCard.isExpired()) {
            throw new BadRequestException("Destination card has expired");
        }

        // Проверяем сумму
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be positive");
        }

        // Проверяем баланс
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds on the source card");
        }

        // Выполняем перевод
        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        // Создаем запись о переводе
        Transfer transfer = Transfer.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransferStatus.COMPLETED)
                .user(currentUser)
                .build();

        Transfer savedTransfer = transferRepository.save(transfer);
        return mapToDTO(savedTransfer);
    }

    public TransferDTO getTransferById(Long id) {
        User currentUser = userService.getCurrentUser();
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));

        // Пользователь может видеть только свои переводы
        if (!transfer.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this transfer");
        }

        return mapToDTO(transfer);
    }

    public Page<TransferDTO> getUserTransfers(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return transferRepository.findByUser(currentUser, pageable).map(this::mapToDTO);
    }

    public Page<TransferDTO> getTransfersByCardId(Long cardId, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        
        // Проверяем, что карта принадлежит пользователю
        Card card = cardService.getCardEntity(cardId);
        if (!card.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this card");
        }

        return transferRepository.findByCardIdAndUserId(cardId, currentUser.getId(), pageable)
                .map(this::mapToDTO);
    }

    public List<TransferDTO> getTransfersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = userService.getCurrentUser();
        
        return transferRepository.findByUserIdAndDateRange(currentUser.getId(), startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TransferDTO> getTransfersByStatus(TransferStatus status) {
        User currentUser = userService.getCurrentUser();
        
        return transferRepository.findByUserIdAndStatus(currentUser.getId(), status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TransferDTO mapToDTO(Transfer transfer) {
        String fromCardDecrypted = encryptionUtil.decrypt(transfer.getFromCard().getCardNumber());
        String toCardDecrypted = encryptionUtil.decrypt(transfer.getToCard().getCardNumber());

        return TransferDTO.builder()
                .id(transfer.getId())
                .fromCardMasked(cardMaskingUtil.maskCardNumber(fromCardDecrypted))
                .toCardMasked(cardMaskingUtil.maskCardNumber(toCardDecrypted))
                .amount(transfer.getAmount())
                .transferDate(transfer.getTransferDate())
                .status(transfer.getStatus())
                .description(transfer.getDescription())
                .build();
    }
}