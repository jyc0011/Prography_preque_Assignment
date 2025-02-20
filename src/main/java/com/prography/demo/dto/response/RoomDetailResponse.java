package com.prography.demo.dto.response;

import com.prography.demo.domain.Room;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDetailResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Integer id;
    private String title;
    private Integer hostId;
    private String roomType;
    private String status;
    private String createdAt;
    private String updatedAt;

    public static RoomDetailResponse fromEntity(Room room) {
        return RoomDetailResponse.builder()
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
