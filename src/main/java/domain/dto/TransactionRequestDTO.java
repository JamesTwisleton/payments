package domain.dto;

import domain.entity.TransactionCurrency;
import java.math.BigDecimal;

public record TransactionRequestDTO(
    String senderId, String recipientId, BigDecimal amount, TransactionCurrency currency) {}
