package com.prography.demo.service;

import com.prography.demo.domain.Room;
import com.prography.demo.domain.UserRoom;
import com.prography.demo.domain.Users;
import com.prography.demo.domain.enumType.RoomStatus;
import com.prography.demo.domain.enumType.RoomType;
import com.prography.demo.domain.enumType.TeamType;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public ApiResponse<Void> createRoom(CreateRoomRequestDto request) {
        // 1) 유저 조회
        Users users = userRepository.findById(request.getUserId()).orElse(null);
        if (users == null || users.getStatus() != UserStatus.ACTIVE) {
            return ApiResponse.onFailure(null);
        }

        // 2) 이미 참여 중인 방 확인
        boolean isUserInRoom = userRoomRepository.findByUsersId(users.getId()).isPresent();
        if (isUserInRoom) {
            return ApiResponse.onFailure(null);
        }

        // 3) 방 생성
        Room room = new Room();
        room.setHost(users.getId());
        room.setTitle(request.getTitle());
        room.setRoom_type(request.getRoomType());
        room.setStatus(RoomStatus.WAIT);
        roomRepository.save(room);

        // 4) 호스트가 UserRoom 생성 + 팀 배정
        TeamType assignedTeam = assignTeam(room, true);  // randomAssign = true
        if (assignedTeam == null) {
            // 이론상 방을 막 만들었으니 정원 꽉 찰 일은 없지만, 혹시나 null이면 실패 처리
            return ApiResponse.onFailure(null);
        }

        UserRoom hostUserRoom = new UserRoom();
        hostUserRoom.setUsers(users);
        hostUserRoom.setRoom(room);
        hostUserRoom.setTeam(assignedTeam);
        userRoomRepository.save(hostUserRoom);

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
        Users users = userRepository.findById(userId).orElse(null);
        if (users == null) {
            return ApiResponse.onFailure(null);
        }

        // 1) 대기(WAIT) 상태 방인지
        if (room.getStatus() != RoomStatus.WAIT) {
            return ApiResponse.onFailure(null);
        }

        // 2) 유저 상태가 ACTIVE 인지
        if (users.getStatus() != UserStatus.ACTIVE) {
            return ApiResponse.onFailure(null);
        }

        // 3) 유저가 이미 참여중인 방이 있는지
        //   => 현재 코드에서 findById(userId)로 체크하는데, 그건 UserRoom PK로 해석될 수 있으므로 주의!
        //      실제론 userRoomRepository.findByUsersId(...) 또는 findByUserId(...)를 써야 합니다.
        boolean isUserInAnotherRoom = userRoomRepository.findByUsersId(users.getId()).isPresent();
        if (isUserInAnotherRoom) {
            return ApiResponse.onFailure(null);
        }

        // 4) 방의 정원이 꽉 찼는지 (SINGLE=2, DOUBLE=4)
        int participantCount = userRoomRepository.countByRoomId(roomId);
        int capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        if (participantCount >= capacity) {
            return ApiResponse.onFailure(null);
        }

        // 5) 팀 배정: 두 팀 모두 여유 있으면 랜덤, 아니면 남는 곳
        TeamType assignedTeam = assignTeam(room, true);
        if (assignedTeam == null) {
            // 팀 양쪽 모두 정원이 찼다면 실패
            return ApiResponse.onFailure(null);
        }

        // 6) 조건 충족 시 UserRoom 생성
        UserRoom userRoom = new UserRoom();
        userRoom.setUsers(users);
        userRoom.setRoom(room);
        userRoom.setTeam(assignedTeam);

        userRoomRepository.save(userRoom);

        return ApiResponse.onSuccess(null);
    }

    @Transactional
    public ApiResponse<Void> leaveRoom(Integer roomId, Integer userId) {
        // 1) 방 존재?
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponse.onFailure(null);
        }

        // 2) 해당 방에 참가 중인지?
        UserRoom userRoom = userRoomRepository.findByUsersIdAndRoomId(userId, roomId).orElse(null);
        if (userRoom == null) {
            return ApiResponse.onFailure(null);
        }

        // 3) 방 상태가 WAIT이 아닌 경우 (PROGRESS or FINISH)
        if (room.getStatus() != RoomStatus.WAIT) {
            return ApiResponse.onFailure(null);
        }

        // 4) 만약 호스트가 나가면, 방에 있던 모두 나가기 → 방 상태=FINISH
        if (room.getHost().equals(userId)) {
            // 방 참가자 전부 삭제
            List<UserRoom> participants = userRoomRepository.findAllByRoomId(roomId);
            userRoomRepository.deleteAllInBatch(participants);

            // 방 상태 FINISH
            room.setStatus(RoomStatus.FINISH);
            roomRepository.save(room);
        } else {
            // 호스트가 아닌 경우, 단순히 본인만 나가기
            userRoomRepository.delete(userRoom);
        }

        return ApiResponse.onSuccess(null);
    }

    private TeamType assignTeam(Room room, boolean randomAssign) {
        // 방 타입에 따라 정원 결정
        int capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        int half = capacity / 2; // SINGLE=1, DOUBLE=2

        // 현재 방에 RED / BLUE 몇 명 있는지 확인
        int redCount = userRoomRepository.countByRoomIdAndTeam(room.getId(), TeamType.RED);
        int blueCount = userRoomRepository.countByRoomIdAndTeam(room.getId(), TeamType.BLUE);

        // 두 팀 다 자리가 있는지 판별
        boolean redHasSpace = (redCount < half);
        boolean blueHasSpace = (blueCount < half);

        if (redHasSpace && blueHasSpace) {
            // 두 팀 모두 여유가 있음
            if (randomAssign) {
                // 랜덤 배정
                return Math.random() < 0.5 ? TeamType.RED : TeamType.BLUE;
            } else {
                // RED 우선 배정 (원하시면 BLUE 우선도 가능)
                return TeamType.RED;
            }
        } else if (redHasSpace) {
            // RED만 여유가 있음
            return TeamType.RED;
        } else if (blueHasSpace) {
            // BLUE만 여유가 있음
            return TeamType.BLUE;
        }
        // 둘 다 꽉 찼다면 null
        return null;
    }
}
