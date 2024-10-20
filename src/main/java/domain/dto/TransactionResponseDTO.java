package domain.dto;

import domain.entity.TransactionStatus;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record TransactionResponseDTO(
    String transactionId,
    String senderId,
    String recipientId,
    BigDecimal amount,
    TransactionStatus status) {}
