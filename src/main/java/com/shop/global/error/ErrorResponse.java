package com.shop.global.error;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
    String code,
    String message,
    LocalDateTime timestamp,
    Object details
) {}
