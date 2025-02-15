package com.prography.demo.controller;

import com.prography.demo.dto.request.CreateRoomRequestDto;
import com.prography.demo.dto.request.ExitRoomRequest;
import com.prography.demo.dto.request.JoinRoomRequest;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@Tag(name = "방 관련 API")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "방 생성", description = "")
    public ApiResponse<Void> createRoom(@RequestBody CreateRoomRequestDto request) {
        return roomService.createRoom(request);
    }

    // 1) 방 전체 조회
    @GetMapping
    @Operation(summary = "방 전체 조회", description = """
        페이징을 위한 size, page 받고, id 기준 오름차순으로 데이터 반환
        """)
    public ApiResponse<Object> getAllRooms(
            @RequestParam int size,
            @RequestParam int page
    ) {
        return roomService.getAllRooms(page, size);
    }

    // 2) 방 상세 조회
    @GetMapping("/{roomId}")
    @Operation(summary = "방 상세 조회", description = """
        roomId에 대한 상세정보 (createdAt, updatedAt 포함)
        """)
    public ApiResponse<?> getRoomDetail(@PathVariable Integer roomId) {
        return roomService.getRoomDetail(roomId);
    }

    // 3) 방 참가 API
    @PostMapping("/attention/{roomId}")
    @Operation(summary = "방 참가", description = """
        - 대기(WAIT) 상태인 방에만 참가 가능<br/>
        - 유저는 ACTIVE 상태여야 함<br/>
        - 이미 다른 방에 참가중이면 불가<br/>
        - 방 정원이 꽉 찼으면 불가 (SINGLE=2, DOUBLE=4)
        """)
    public ApiResponse<?> joinRoom(
            @PathVariable Integer roomId,
            @RequestBody JoinRoomRequest body
    ) {
        return roomService.joinRoom(roomId, body.getUserId());
    }


    @PostMapping("/out/{roomId}")
    @Operation(summary = "방 나가기 API", description = """
        - 유저가 방에 참가중이어야 나가기 가능<br/>
        - 방 상태가 WAIT이어야 가능<br/>
        - host가 나가면 전체 퇴장 + 방 상태 FINISH
        """)
    public ApiResponse<Void> leaveRoom(@PathVariable Integer roomId, @RequestBody ExitRoomRequest body) {
        return roomService.leaveRoom(roomId, body.getUserId());
    }
}