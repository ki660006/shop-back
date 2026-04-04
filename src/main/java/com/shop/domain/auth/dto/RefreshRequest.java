package com.shop.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.shop.domain.auth.entity.RefreshToken;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    @NotBlank
    private String refreshToken;
}
