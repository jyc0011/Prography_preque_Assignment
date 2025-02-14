package com.prography.demo.dto.request;

import com.prography.demo.domain.enumType.RoomType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private Integer userId;
    private String title;
    private RoomType roomType;
}