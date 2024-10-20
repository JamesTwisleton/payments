package domain.dto;

import lombok.Builder;

@Builder
public record AccountDTO(String accountId, String balance) {}
