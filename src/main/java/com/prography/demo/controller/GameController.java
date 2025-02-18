package com.prography.demo.controller;

import com.prography.demo.dto.request.ChangeTeamRequest;
import com.prography.demo.dto.request.GameStartRequest;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.service.GameService;
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
@RequiredArgsConstructor
@Tag(name = "게임 시작 API")
public class GameController {

    private final GameService gameService;

    @PutMapping("/room/start/{roomId}")
    @Operation(summary = "게임시작 API", description = """
        hostUserId만 게임 시작 가능.<br/>
        방 정원이 꽉 차있어야 하고, 상태가 WAIT여야 함.<br/>
        1분 뒤 자동 FINISH
        """)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "게임 시작 요청 객체. **userId**를 포함합니다.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = GameStartRequest.class),
                    examples = @ExampleObject(
                            name = "GameStartExample",
                            value = "{\n  \"userId\": 1\n}"
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
    public ApiResponse<Void> startGame(
            @Parameter(description = "게임 시작할 방의 ID", example = "1") @PathVariable Integer roomId,
            @RequestBody GameStartRequest request
    ) {
        return gameService.startGame(roomId, request.getUserId());
    }

    @PutMapping("/team/{roomId}")
    @Operation(summary = "팀 변경 API", description = """
        - 같은 방에 참가 중인 유저만 팀 변경 가능<br/>
        - 반대팀이 이미 정원의 절반이면 변경 불가<br/>
        - 방 상태가 WAIT 여야 가능
        """)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "팀 변경 요청 객체. **userId**를 포함합니다.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = ChangeTeamRequest.class),
                    examples = @ExampleObject(
                            name = "ChangeTeamExample",
                            value = "{\n  \"userId\": 1\n}"
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
    public ApiResponse<Void> changeTeam(
            @Parameter(description = "팀 변경할 방의 ID", example = "1") @PathVariable Integer roomId,
            @RequestBody ChangeTeamRequest body
    ) {
        return gameService.changeTeam(roomId, body.getUserId());
    }
}