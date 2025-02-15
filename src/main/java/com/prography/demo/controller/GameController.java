package com.prography.demo.controller;

import com.prography.demo.dto.request.ChangeTeamRequest;
import com.prography.demo.dto.request.GameStartRequest;
import com.prography.demo.service.GameService;
import org.springframework.web.bind.annotation.PutMapping;
import com.prography.demo.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@Tag(name = "게임 시작 API")
public class GameController {

    private final GameService gameService;

    @PutMapping("/start/{roomId}")
    @Operation(summary = "게임시작 API", description = """
        hostUserId만 게임 시작 가능.<br/>
        방 정원이 꽉 차있어야 하고, 상태가 WAIT여야 함.<br/>
        1분 뒤 자동 FINISH
        """)
    public ApiResponse<Void> startGame(@PathVariable Integer roomId, @RequestBody GameStartRequest request) {
        return gameService.startGame(roomId, request.getUserId());
    }

    @PutMapping("/{roomId}")
    @Operation(summary = "팀 변경 API", description = """
        - 같은 방에 참가 중인 유저만 팀 변경 가능<br/>
        - 반대팀이 이미 정원의 절반이면 변경 불가<br/>
        - 방 상태가 WAIT 여야 가능
        """)
    public ApiResponse<Void> changeTeam(@PathVariable Integer roomId, @RequestBody ChangeTeamRequest body) {
        return gameService.changeTeam(roomId, body.getUserId());
    }
}