package com.prography.demo.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값인 경우 해당 필드는 Json, 즉 Response에서 숨기게 함.
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T result;

    // 성공
    public static <T> ApiResponse<T> onSuccess(T result) {
        return new ApiResponse<>(200,  "API 요청이 성공했습니다.", result);
    }

    // 실패
    public static <T> ApiResponse<T> onFailure(T result) {
        return new ApiResponse<>(201, "불가능한 요청입니다.", result);
    }
}