package com.prography.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography.demo.domain.enumType.RoomType;
import com.prography.demo.dto.request.CreateRoomRequestDto;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Test
    @DisplayName("방 생성 - 성공 시 code=200, message=API 요청이 성공했습니다.")
    void createRoom_success() throws Exception {
        // given
        CreateRoomRequestDto dto = new CreateRoomRequestDto(1, RoomType.SINGLE, "테스트방");

        // RoomService.createRoom() → success
        when(roomService.createRoom(any(CreateRoomRequestDto.class)))
                .thenReturn(ApiResponse.onSuccess(null));

        // when & then
        mockMvc.perform(
                        post("/room")
                                .contentType("application/json")
                                .content(new ObjectMapper().writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("API 요청이 성공했습니다."));
    }

    @Test
    @DisplayName("방 생성 - 유저가 ACTIVE 상태가 아닐 때 code=201")
    void createRoom_fail_notActiveUser() throws Exception {
        // given
        CreateRoomRequestDto dto = new CreateRoomRequestDto(2, RoomType.DOUBLE, "비활성유저");

        // RoomService.createRoom() → failure(201)
        when(roomService.createRoom(any(CreateRoomRequestDto.class)))
                .thenReturn(ApiResponse.onFailure(null));

        // when & then
        mockMvc.perform(
                        post("/room")
                                .contentType("application/json")
                                .content(new ObjectMapper().writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value(null));
    }

    @Test
    @DisplayName("방 전체 조회 - 성공 응답")
    void getAllRooms_success() throws Exception {
        // given
        var resultMap = Map.of(
                "totelElements", 2L,
                "totalPages", 1,
                "roomList", new Object[] {}
        );
        // mocking
        when(roomService.getAllRooms(anyInt(), anyInt()))
                .thenReturn(ApiResponse.onSuccess(resultMap));

        // when & then
        mockMvc.perform(get("/room")
                        .param("size","10")
                        .param("page","0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("API 요청이 성공했습니다."))
                .andExpect(jsonPath("$.result.totelElements").value(2))
                .andExpect(jsonPath("$.result.totalPages").value(1));
    }

    @Test
    @DisplayName("방 상세 조회 - 존재하지 않는 방")
    void getRoomDetail_notExists() throws Exception {
        // given
        when(roomService.getRoomDetail(999))
                .thenReturn(ApiResponse.onFailure(null));

        // when & then
        mockMvc.perform(get("/room/999"))
                .andExpect(status().isOk()) // HTTP는 200이지만, body.code=201
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value(null));
    }
}
