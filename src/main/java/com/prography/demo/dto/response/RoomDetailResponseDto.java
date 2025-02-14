package com.prography.demo.dto.response;

import com.prography.demo.domain.Room;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDetailResponseDto {

    private Integer id;
    private String title;
    private Integer hostId;
    private String roomType;
    private String status;
    private String createdAt;
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static RoomDetailResponseDto fromEntity(Room room) {
        return RoomDetailResponseDto.builder()
                .id(room.getId())
                .title(room.getTitle())
                .hostId(room.getHost())
                .roomType(room.getRoom_type().name())
                .status(room.getStatus().name())
                .createdAt(room.getCreated_at().format(FORMATTER))
                .updatedAt(room.getUpdated_at().format(FORMATTER))
                .build();
    }
}
