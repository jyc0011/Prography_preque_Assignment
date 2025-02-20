package com.prography.demo.dto.request;

import com.prography.demo.domain.enumType.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {

    @Schema(description = "방을 생성하는 유저의 ID", example = "1")
    private Integer userId;

    @Schema(description = "방 타입 (SINGLE or DOUBLE)", example = "SINGLE")
    private RoomType roomType;

    @Schema(description = "방 제목", example = "오늘 저녁 9시 게임합니다")
    private String title;
}