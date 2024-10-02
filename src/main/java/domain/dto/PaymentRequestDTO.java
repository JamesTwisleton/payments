package domain.dto;

import domain.entity.PaymentCurrency;
import java.math.BigDecimal;

public record PaymentRequestDTO(
    String payerId, String recipientId, BigDecimal amount, PaymentCurrency currency) {}
