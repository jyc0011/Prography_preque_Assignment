package com.prography.demo.controller;

import com.prography.demo.dto.response.UserResponseDto;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@Tag(name = "유저 API")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @GetMapping("/user")
    @Operation(summary = "유저 전체 조회 API",
            description = "페이징 처리를 위한 size, page 값을 RequestParameter로 받습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse
                    (responseCode = "200", description = "API 요청이 성공했습니다.")})
    public ApiResponse<Map<String, Object>> getAllUsers(@RequestParam int size, @RequestParam int page) {
        Page<UserResponseDto> userPage = userService.getAllUsers(page, size);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("totalElements", userPage.getTotalElements());
        responseBody.put("totalPages", userPage.getTotalPages() - 1);
        responseBody.put("userList", userPage.getContent());
        return ApiResponse.onSuccess(responseBody);
    }
}
