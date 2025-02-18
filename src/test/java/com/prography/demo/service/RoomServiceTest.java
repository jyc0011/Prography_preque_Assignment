package com.prography.demo.service;

import com.prography.demo.domain.Room;
import com.prography.demo.domain.enumType.RoomStatus;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.repository.RoomRepository;
import com.prography.demo.repository.UserRepository;
import com.prography.demo.repository.UserRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;

// 간단 예시. 실제론 @ExtendWith(MockitoExtension.class) + @InjectMocks 사용도 가능
@SpringBootTest
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoomRepository userRoomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    @DisplayName("방 상세 조회 - 존재하지 않는 방이면 code=201")
    void getRoomDetail_notExist() {
        // given
        Mockito.when(roomRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        // when
        ApiResponse<?> result = roomService.getRoomDetail(999);

        // then
        assertThat(result.getCode()).isEqualTo(201);
        assertThat(result.getMessage()).isEqualTo("존재하지 않는 방입니다.");
    }

    @Test
    @DisplayName("방 상세 조회 - 존재하는 방이면 code=200")
    void getRoomDetail_exist() {
        // given
        Room room = new Room();
        room.setId(1);
        room.setTitle("TestRoom");
        room.setStatus(RoomStatus.WAIT);
        Mockito.when(roomRepository.findById(1))
                .thenReturn(Optional.of(room));

        // when
        ApiResponse<?> result = roomService.getRoomDetail(1);

        // then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("API 요청이 성공했습니다.");
        // ...
    }
}
