package domain.dto;

import domain.entity.PaymentCurrency;
import domain.entity.TransactionStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionResponseDTO(
        String transactionId, String senderId, String recipientId, BigDecimal amount, TransactionStatus status) {}
