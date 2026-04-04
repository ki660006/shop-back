package com.shop.domain.catalog.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorResponse<T> {
    private List<T> content;
    private String nextCursor;
    private boolean hasNext;
}
