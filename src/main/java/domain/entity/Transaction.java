package domain.entity;

import domain.dto.TransactionResponseDTO;
import java.math.BigDecimal;
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
