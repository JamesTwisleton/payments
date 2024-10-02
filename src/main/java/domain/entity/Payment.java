package domain.entity;

import static domain.entity.PaymentStatus.FAILED;
import static domain.entity.PaymentStatus.PENDING;
import static domain.entity.PaymentStatus.SUCCESS;

import domain.dto.PaymentRequestDTO;
import domain.dto.PaymentResponseDTO;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Payment(
    String paymentId,
    String payerId,
    String recipientId,
    BigDecimal amount,
    PaymentCurrency currency,
    PaymentStatus status) {

  public static Payment fromRequestDTO(PaymentRequestDTO paymentRequestDTO) {
    return Payment.builder()
        .paymentId(UUID.randomUUID().toString()) // Generate unique ID
        .payerId(paymentRequestDTO.payerId())
        .recipientId(paymentRequestDTO.recipientId())
        .amount(paymentRequestDTO.amount())
        .currency(paymentRequestDTO.currency())
        .status(PENDING) // default status
        .build();
  }

  // Convert a Payment entity to a PaymentResponseDto
  public PaymentResponseDTO toResponseDto() {
    return PaymentResponseDTO.builder()
        .paymentId(this.paymentId)
        .payerId(this.payerId)
        .recipientId(this.recipientId)
        .amount(this.amount)
        .currency(this.currency)
        .status(this.status)
        .build();
  }

  // Method to mark the payment as successful
  public Payment markSuccess() {
    return Payment.builder()
        .paymentId(paymentId)
        .payerId(payerId)
        .recipientId(recipientId)
        .amount(amount)
        .currency(currency)
        .status(SUCCESS)
        .build();
  }

  // Method to mark the payment as failed
  public Payment markFailed() {
    return Payment.builder()
        .paymentId(paymentId)
        .payerId(payerId)
        .recipientId(recipientId)
        .amount(amount)
        .currency(currency)
        .status(FAILED)
        .build();
  }
}
