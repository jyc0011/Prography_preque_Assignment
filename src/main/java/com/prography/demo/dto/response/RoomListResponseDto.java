package com.prography.demo.dto.response;

import com.prography.demo.domain.Room;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomListResponseDto {
    private Integer id;
    private String title;
    private Integer hostId;
    private String roomType;
    private String status;

    public static RoomListResponseDto fromEntity(Room room) {
        return RoomListResponseDto.builder()
                .id(room.getId())
                .title(room.getTitle())
                .hostId(room.getHost())
                .roomType(room.getRoom_type().name())
                .status(room.getStatus().name())
                .build();
    }
}
