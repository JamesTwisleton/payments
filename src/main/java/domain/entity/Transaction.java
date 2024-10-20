package domain.entity;

import static domain.entity.PaymentStatus.FAILED;
import static domain.entity.PaymentStatus.PENDING;
import static domain.entity.PaymentStatus.SUCCESS;

import domain.dto.PaymentRequestDTO;
import domain.dto.PaymentResponseDTO;
import java.math.BigDecimal;
import java.util.UUID;

import domain.dto.TransactionResponseDTO;
import lombok.Builder;

@Builder
public record Transaction(
    String transactionId,
    String senderId,
    String recipientId,
    BigDecimal amount,
    TransactionStatus status) {

    // Convert a Payment entity to a PaymentResponseDto
    public TransactionResponseDTO toResponseDto() {
        return TransactionResponseDTO.builder()
                .transactionId(this.transactionId)
                .senderId(this.senderId)
                .recipientId(this.recipientId)
                .amount(this.amount)
                .status(this.status)
                .build();
    }
}
