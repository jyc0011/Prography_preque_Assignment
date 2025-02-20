package com.prography.demo.service;

import com.prography.demo.domain.Room;
import com.prography.demo.domain.UserRoom;
import com.prography.demo.domain.Users;
import com.prography.demo.domain.enumType.RoomStatus;
import com.prography.demo.domain.enumType.RoomType;
import com.prography.demo.domain.enumType.TeamType;
import com.prography.demo.domain.enumType.UserStatus;
import com.prography.demo.dto.request.CreateRoomRequest;
import com.prography.demo.dto.response.RoomDetailResponse;
import com.prography.demo.dto.response.RoomListResponse;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.repository.RoomRepository;
import com.prography.demo.repository.UserRepository;
import com.prography.demo.repository.UserRoomRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public ApiResponse<Void> createRoom(CreateRoomRequest request) {
        Users users = userRepository.findById(request.getUserId()).orElse(null);
        if (users == null) {
            log.error("createRoom() 실패: userId={}인 유저가 존재하지 않습니다.", request.getUserId());
            return ApiResponse.onFailure(null);
        }
        if (users.getStatus() != UserStatus.ACTIVE) {
            log.error("createRoom() 실패: userId={} 유저 상태가 ACTIVE가 아닙니다. status={}", users.getId(), users.getStatus());
            return ApiResponse.onFailure(null);
        }
        boolean isUserInRoom = userRoomRepository.findByUsersId(users.getId()).isPresent();
        if (isUserInRoom) {
            log.error("createRoom() 실패: userId={} 이미 다른 방에 참여중입니다.", users.getId());
            return ApiResponse.onFailure(null);
        }
        Room room = new Room();
        room.setHost(users.getId());
        room.setTitle(request.getTitle());
        room.setRoom_type(request.getRoomType());
        room.setStatus(RoomStatus.WAIT);
        roomRepository.save(room);
        TeamType assignedTeam = assignTeam(room);
        if (assignedTeam == null) {
            log.error("createRoom() 실패: team 배정 불가 (roomId={})", room.getId());
            return ApiResponse.onFailure(null);
        }

        UserRoom hostUserRoom = new UserRoom();
        hostUserRoom.setUsers(users);
        hostUserRoom.setRoom(room);
        hostUserRoom.setTeam(assignedTeam);
        userRoomRepository.save(hostUserRoom);

        return ApiResponse.onSuccess(null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Object> getAllRooms(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Room> roomPage = roomRepository.findAll(pageable);
        Page<RoomListResponse> dtoPage = roomPage.map(RoomListResponse::fromEntity);

        int totalElements = (int) dtoPage.getTotalElements();
        int totalPages = dtoPage.getTotalPages();

        var resultMap = new HashMap<String, Object>();
        resultMap.put("totelElements", totalElements);
        resultMap.put("totalPages", totalPages);
        resultMap.put("roomList", dtoPage.getContent());

        return ApiResponse.onSuccess(resultMap);
    }

    @Transactional(readOnly = true)
    public ApiResponse<RoomDetailResponse> getRoomDetail(Integer roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.error("getRoomDetail() 실패: roomId={}인 방이 존재하지 않습니다.", roomId);
            return ApiResponse.onFailure(null);
        }
        RoomDetailResponse dto = RoomDetailResponse.fromEntity(room);
        return ApiResponse.onSuccess(dto);
    }

    @Transactional
    public ApiResponse<Void> joinRoom(Integer roomId, Integer userId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.error("joinRoom() 실패: roomId={}인 방이 존재하지 않습니다.", roomId);
            return ApiResponse.onFailure(null);
        }
        Users users = userRepository.findById(userId).orElse(null);
        if (users == null) {
            log.error("joinRoom() 실패: userId={}인 유저가 존재하지 않습니다.", userId);
            return ApiResponse.onFailure(null);
        }
        if (room.getStatus() != RoomStatus.WAIT) {
            log.error("joinRoom() 실패: roomId={} 방 상태가 WAIT가 아님. status={}", roomId, room.getStatus());
            return ApiResponse.onFailure(null);
        }
        if (users.getStatus() != UserStatus.ACTIVE) {
            log.error("joinRoom() 실패: userId={} 유저 상태가 ACTIVE가 아님. status={}", userId, users.getStatus());
            return ApiResponse.onFailure(null);
        }
        boolean isUserInAnotherRoom = userRoomRepository.findByUsersId(users.getId()).isPresent();
        if (isUserInAnotherRoom) {
            log.error("joinRoom() 실패: userId={} 이미 다른 방에 참여중.", userId);
            return ApiResponse.onFailure(null);
        }
        int participantCount = userRoomRepository.countByRoomId(roomId);
        int capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        if (participantCount >= capacity) {
            log.error("joinRoom() 실패: roomId={} 정원 초과 (capacity={})", roomId, capacity);
            return ApiResponse.onFailure(null);
        }

        TeamType assignedTeam = assignTeam(room);
        if (assignedTeam == null) {
            log.error("joinRoom() 실패: team 배정 불가 (roomId={}, userId={})", roomId, userId);
            return ApiResponse.onFailure(null);
        }

        UserRoom userRoom = new UserRoom();
        userRoom.setUsers(users);
        userRoom.setRoom(room);
        userRoom.setTeam(assignedTeam);
        userRoomRepository.save(userRoom);

        return ApiResponse.onSuccess(null);
    }

    @Transactional
    public ApiResponse<Void> leaveRoom(Integer roomId, Integer userId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.error("leaveRoom() 실패: roomId={} 방이 존재하지 않습니다.", roomId);
            return ApiResponse.onFailure(null);
        }
        UserRoom userRoom = userRoomRepository.findByUsersIdAndRoomId(userId, roomId).orElse(null);
        if (userRoom == null) {
            log.error("leaveRoom() 실패: userId={}은 roomId={} 방에 참가중이지 않습니다.", userId, roomId);
            return ApiResponse.onFailure(null);
        }
        if (room.getStatus() != RoomStatus.WAIT) {
            log.error("leaveRoom() 실패: 방 상태가 WAIT가 아님. roomId={}, status={}", roomId, room.getStatus());
            return ApiResponse.onFailure(null);
        }
        if (room.getHost().equals(userId)) {
            List<UserRoom> participants = userRoomRepository.findAllByRoomId(roomId);
            userRoomRepository.deleteAllInBatch(participants);
            room.setStatus(RoomStatus.FINISH);
            roomRepository.save(room);
            log.info("leaveRoom() 성공: 호스트(userId={})가 방(roomId={})을 나감 -> 방 상태 FINISH 처리", userId, roomId);
        } else {
            userRoomRepository.delete(userRoom);
            log.info("leaveRoom() 성공: userId={} 방(roomId={}) 나가기 처리 완료", userId, roomId);
        }

        return ApiResponse.onSuccess(null);
    }

    /**
     * 방 정원 및 현재 RED/BLUE 인원을 확인하여 팀 배정.
     * - 두 팀 모두 여유 있으면 랜덤
     * - 한 쪽만 여유 있으면 그 쪽
     * - 양쪽 다 꽉 차면 null
     */
    private TeamType assignTeam(Room room) {
        int capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        int half = capacity / 2;

        int redCount = userRoomRepository.countByRoomIdAndTeam(room.getId(), TeamType.RED);
        int blueCount = userRoomRepository.countByRoomIdAndTeam(room.getId(), TeamType.BLUE);

        boolean redHasSpace = (redCount < half);
        boolean blueHasSpace = (blueCount < half);

        if (redHasSpace && blueHasSpace) {
            return Math.random() < 0.5 ? TeamType.RED : TeamType.BLUE;
        } else if (redHasSpace) {
            return TeamType.RED;
        } else if (blueHasSpace) {
            return TeamType.BLUE;
        }
        return null;
    }
}