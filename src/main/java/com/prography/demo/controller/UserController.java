package com.prography.demo.controller;

import com.prography.demo.dto.response.UserResponseDto;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping
@Tag(name = "유저 API")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @GetMapping("/user")
    @Operation(summary = "유저 전체 조회 API",
            description = "모든 회원 정보 응답, id 기준 오름차순")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "201", description = "불가능한 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "500", description = "에러가 발생했습니다.")})
    public ApiResponse<Map<String, Object>> getAllUsers(@Parameter(description = "페이지 크기", example = "10") @RequestParam int size,
                                                        @Parameter(description = "현재 페이지 번호", example = "0") @RequestParam int page) {
        Page<UserResponseDto> userPage = userService.getAllUsers(page, size);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("totalElements", userPage.getTotalElements());
        responseBody.put("totalPages", userPage.getTotalPages());
        responseBody.put("userList", userPage.getContent());
        return ApiResponse.onSuccess(responseBody);
    }
}
