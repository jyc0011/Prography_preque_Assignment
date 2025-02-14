package com.prography.demo.service;

import com.prography.demo.domain.Room;
import com.prography.demo.domain.User;
import com.prography.demo.domain.UserRoom;
import com.prography.demo.domain.enumType.RoomStatus;
import com.prography.demo.domain.enumType.RoomType;
import com.prography.demo.domain.enumType.UserStatus;
import com.prography.demo.dto.request.CreateRoomRequestDto;
import com.prography.demo.dto.response.RoomDetailResponseDto;
import com.prography.demo.dto.response.RoomListResponseDto;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.repository.RoomRepository;
import com.prography.demo.repository.UserRepository;
import com.prography.demo.repository.UserRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public ApiResponse<Void> createRoom(CreateRoomRequestDto request) {
        // 1) 유저 조회
        User user = userRepository.findById(request.getUserId())
                .orElse(null);

        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            // 활성 상태가 아니라면
            return ApiResponse.onFailure(null);
        }

        // 2) 이미 참여 중인 방 확인
        boolean isUserInRoom = userRoomRepository.findByUser_id(user.getId()).isPresent();
        if (isUserInRoom) {
            return ApiResponse.onFailure(null);
        }

        // 3) 방 생성
        Room room = new Room();
        room.setHost(user.getId());
        room.setTitle(request.getTitle());
        room.setRoom_type(request.getRoomType());
        room.setStatus(RoomStatus.WAIT);

        roomRepository.save(room);

        return ApiResponse.onSuccess(null);
    }


    // ----------------------
    // 1) 방 전체 조회
    // ----------------------
    @Transactional(readOnly = true)
    public ApiResponse<Object> getAllRooms(int page, int size) {
        // id 오름차순 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Room> roomPage = roomRepository.findAll(pageable);
        Page<RoomListResponseDto> dtoPage = roomPage.map(RoomListResponseDto::fromEntity);

        // 문제에서 요구하는 응답 구조: { "totalElements", "totalPages", "roomList": [] }
        Integer totalElements = (int) dtoPage.getTotalElements();
        int totalPages = dtoPage.getTotalPages();

        var resultMap = new java.util.HashMap<String,Object>();
        resultMap.put("totelElements", totalElements);
        resultMap.put("totalPages", totalPages);
        resultMap.put("roomList", dtoPage.getContent());

        return ApiResponse.onSuccess(resultMap);
    }

    // ----------------------
    // 2) 방 상세 조회
    // ----------------------
    @Transactional(readOnly = true)
    public ApiResponse<RoomDetailResponseDto> getRoomDetail(Integer roomId) {
        // 존재하지 않는 roomId => code=201
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponse.onFailure(null);
        }

        RoomDetailResponseDto dto = RoomDetailResponseDto.fromEntity(room);
        return ApiResponse.onSuccess(dto);
    }

    // ----------------------
    // 3) 방 참가
    // ----------------------
    @Transactional
    public ApiResponse<Void> joinRoom(Integer roomId, Integer userId) {
        // 0) room / user 존재 여부 체크
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponse.onFailure(null);
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.onFailure(null);
        }

        // 1) 대기(WAIT) 상태 방인지
        if (room.getStatus() != RoomStatus.WAIT) {
            return ApiResponse.onFailure(null);
        }

        // 2) 유저 상태가 ACTIVE 인지
        if (user.getStatus() != UserStatus.ACTIVE) {
            return ApiResponse.onFailure(null);
        }

        // 3) 유저가 이미 참여중인 방이 있는지
        boolean isUserInAnotherRoom = userRoomRepository.findById(userId).isPresent();
        if (isUserInAnotherRoom) {
            return ApiResponse.onFailure(null);
        }

        // 4) 방의 정원이 꽉 찼는지 (SINGLE=2, DOUBLE=4)
        Integer participantCount = userRoomRepository.countByRoom_id(roomId);
        Integer capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        if (participantCount >= capacity) {
            return ApiResponse.onFailure(null);
        }

        // 조건 충족 시 UserRoom 생성
        UserRoom userRoom = new UserRoom();
        userRoom.setUser_id(userId);
        userRoom.setRoom_id(roomId);
        userRoomRepository.save(userRoom);

        return ApiResponse.onSuccess(null);
    }
}
