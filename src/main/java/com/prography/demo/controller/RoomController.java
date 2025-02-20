package com.prography.demo.controller;

import com.prography.demo.dto.request.CreateRoomRequest;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@Tag(name = "방 관련 API")
public class RoomController {

    private final RoomService roomService;

    // 방 생성하기
    @PostMapping
    @Operation(summary = "방 생성 API",
            description = """
                    방을 생성하려고 하는 user(userId)의 상태가 활성(ACTIVE)상태일 때만 방 생성.<br>
                    만약 활성상태가 아닐때는 201 응답을 반환.<br>
                    방을 생성하려고 하는 user(userId)가 현재 참여한 방이 있다면, 방을 생성할 수 없음.<br>
                    만약 참여하고 있는 방이 있을때는 201 응답을 반환.<br>
                    방은 초기에 대기(WAIT) 상태로 생성.<br>
                    데이터가 저장되는 시점에 따라 createdAt과 updatedAt을 저장.""")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "방 생성 요청. userId, roomType, title 정보를 포함",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateRoomRequest.class),
                    examples = @ExampleObject(
                            name = "CreateRoomExample",
                            value = """
                                    {
                                        "userId": 1,
                                        "roomType": "NORMAL",
                                        "title": "Sample Room Title"
                                    }"""
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "201", description = "불가능한 요청입니다.")})
    public ApiResponse<Void> createRoom(@RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request);
    }

    // 방 전체 조회
    @GetMapping
    @Operation(summary = "방 전체 조회 API",
            description = """
                    모든 방에 대한 데이터를 반환.<br>
                    id 기준 오름차순으로 데이터를 반환.""")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "201", description = "불가능한 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "500", description = "에러가 발생했습니다.")})
    public ApiResponse<Object> getAllRooms(
            @Parameter(description = "페이지 당 방의 수", example = "10") @RequestParam int size,
            @Parameter(description = "요청할 페이지 번호 (0부터 시작)", example = "0") @RequestParam int page
    ) {
        return roomService.getAllRooms(page, size);
    }

    // 방 상세 조회
    @GetMapping("/{roomId}")
    @Operation(summary = "방 상세 조회 API",
            description = "roomId를 받아 방에 대한 상세 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "201", description = "불가능한 요청입니다.")})
    public ApiResponse<?> getRoomDetail(@Parameter(description = "조회할 방의 ID", example = "1") @PathVariable Integer roomId
    ) {
        return roomService.getRoomDetail(roomId);
    }

    //방 참가 API
    @PostMapping("/attention/{roomId}")
    @Operation(summary = "방 참가 API",
            description = """
                    대기(WAIT) 상태인 방에만 참가 가능<br>
                    유저는 ACTIVE 상태여야 함<br>
                    이미 다른 방에 참가중이면 불가<br>
                    방 정원이 꽉 찼으면 불가 (SINGLE=2, DOUBLE=4)
                    """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "201", description = "불가능한 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "500", description = "에러가 발생했습니다.")})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "방 참가 요청. userId를 포함.",
            required = true,
            content = @Content(
                    examples = @ExampleObject(
                            name = "JoinRoomExample",
                            value = """
                                    {
                                        "userId": 1
                                    }"""
                    )
            )
    )
    public ApiResponse<?> joinRoom(
            @Parameter(description = "참가할 방의 ID", example = "1") @PathVariable Integer roomId,
            @RequestBody Integer userId
    ) {
        return roomService.joinRoom(roomId, userId);
    }

    // 방 나가기
    @PostMapping("/out/{roomId}")
    @Operation(summary = "방 나가기 API",
            description = """
                    유저가 방에 참가중이어야<br>
                    방 상태가 WAIT이어야<br>
                    host가 나가면 전체 퇴장<br>
                    방 상태 FINISH""")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "방 나가기 요청. userId 포함.",
            required = true,
            content = @Content(
                    examples = @ExampleObject(
                            name = "ExitRoomExample",
                            value = """
                                    {
                                        "userId": 1
                                    }"""
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "201", description = "불가능한 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "500", description = "에러가 발생했습니다.")})
    public ApiResponse<Void> leaveRoom(
            @Parameter(description = "퇴장할 방의 ID", example = "1") @PathVariable Integer roomId,
            @RequestBody Integer userId
    ) {
        return roomService.leaveRoom(roomId, userId);
    }
}