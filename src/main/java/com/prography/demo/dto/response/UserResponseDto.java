package com.prography.demo.dto.response;

import com.prography.demo.domain.User;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Integer id;
    private Integer fakerId;
    private String name;
    private String email;
    private String status;
    private String createdAt;
    private String updatedAt;

    public static UserResponseDto fromEntity(User user) {
        // 날짜 포맷: yyyy-MM-dd HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return UserResponseDto.builder()
                .id(user.getId())
                .fakerId(user.getFakerId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .createdAt(user.getCreated_at().format(formatter))
                .updatedAt(user.getUpdated_at().format(formatter))
                .build();
    }
}
