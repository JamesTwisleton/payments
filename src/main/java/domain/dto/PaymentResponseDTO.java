package domain.dto;

import domain.entity.PaymentCurrency;
import domain.entity.PaymentStatus;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentResponseDTO(
    String paymentId,
    String payerId,
    String recipientId,
    BigDecimal amount,
    PaymentCurrency currency,
    PaymentStatus status) {}
