package com.prography.demo.dto.response;

import com.prography.demo.domain.Users;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Integer id;
    private Integer fakerId;
    private String name;
    private String email;
    private String status;
    private String createdAt;
    private String updatedAt;

    public static UserResponse fromEntity(Users users) {
        // 날짜 포맷: yyyy-MM-dd HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return UserResponse.builder()
                .id(users.getId())
                .fakerId(users.getFakerId())
                .name(users.getName())
                .email(users.getEmail())
                .status(users.getStatus().name())
                .createdAt(users.getCreated_at().format(formatter))
                .updatedAt(users.getUpdated_at().format(formatter))
                .build();
    }
}
