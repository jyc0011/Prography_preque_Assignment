package com.prography.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitRequest {
    @Schema(description = "유저 추가를 위해 제공 받는 seed 값", example = "12345")
    private int seed;
    @Schema(description = "유저 추가를 위해 제공 받는 quantity 값", example = "10")
    private int quantity;
}
