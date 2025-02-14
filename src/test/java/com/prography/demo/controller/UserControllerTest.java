package com.prography.demo.controller;

import com.prography.demo.dto.response.UserResponseDto;
import com.prography.demo.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("유저 전체 조회 API - 정상 응답")
    void getAllUsers_success() throws Exception {
        // given
        int page = 0;
        int size = 5;

        List<UserResponseDto> mockList = List.of(
                UserResponseDto.builder()
                        .id(1L).fakerId(1).name("jungran.gwon").email("knam@yahoo.com")
                        .status("ACTIVE").createdAt("2025-02-15 12:00:00").updatedAt("2025-02-15 12:00:00")
                        .build(),
                UserResponseDto.builder()
                        .id(2L).fakerId(2).name("myungho.lim").email("go.minji@hotmail.com")
                        .status("ACTIVE").createdAt("2025-02-15 12:00:00").updatedAt("2025-02-15 12:00:00")
                        .build()
        );

        Page<UserResponseDto> mockPage = new PageImpl<>(mockList, PageRequest.of(page, size), mockList.size());

        Mockito.when(userService.getAllUsers(page, size)).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/user")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("API 요청이 성공했습니다."))
                .andExpect(jsonPath("$.result.totalElements").value(2))
                .andExpect(jsonPath("$.result.totalPages").value(0))
                .andExpect(jsonPath("$.result.userList[0].id").value(1))
                .andExpect(jsonPath("$.result.userList[0].name").value("jungran.gwon"))
                .andExpect(jsonPath("$.result.userList[1].email").value("go.minji@hotmail.com"));
    }
}
