package domain.dto;

import domain.entity.PaymentCurrency;
import java.math.BigDecimal;

public record TransactionRequestDTO(
    String senderId, String recipientId, BigDecimal amount, PaymentCurrency currency) {}
