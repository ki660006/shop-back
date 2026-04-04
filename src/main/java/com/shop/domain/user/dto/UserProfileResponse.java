package com.shop.domain.user.dto;

import com.shop.domain.user.entity.Role;
import lombok.*;
import com.shop.domain.user.entity.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private String address;
    private Role role;
}
