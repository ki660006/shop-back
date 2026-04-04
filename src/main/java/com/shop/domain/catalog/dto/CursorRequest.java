package com.shop.domain.catalog.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorRequest {
    private String cursor; // Base64 encoded string
    private Integer size; // Page size (limit)

    public int getSize() {
        return (size == null || size <= 0) ? 10 : size;
    }
}
