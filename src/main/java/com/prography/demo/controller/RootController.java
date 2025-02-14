package com.prography.demo.controller;

import com.prography.demo.dto.request.InitRequest;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.service.RootService;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Root Controller", description = "기본 동작 API")
public class RootController {
    private final RootService rootService;

    @GetMapping("/health")
    @Operation(summary = "헬스 체크 API",
            description = "서버의 상태를 체크하는 API입니다. 모든 시나리오에 대해 최초 1회 호출됩니다")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다.")})
    public ApiResponse<Void> healthCheck() {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/init")
    @Operation(summary = "초기화 API", description = "seed와 quantity를 body에 담아서 요청합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다.")})
    public ApiResponse<Void> initDatabase(@RequestBody InitRequest request) {
        rootService.initDatabase(request.getSeed(), request.getQuantity());
        return ApiResponse.onSuccess(null);
    }
}
