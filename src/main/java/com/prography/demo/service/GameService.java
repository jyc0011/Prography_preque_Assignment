package com.prography.demo.service;

import com.prography.demo.domain.Room;
import com.prography.demo.domain.UserRoom;
import com.prography.demo.domain.enumType.RoomStatus;
import com.prography.demo.domain.enumType.RoomType;
import com.prography.demo.domain.enumType.TeamType;
import com.prography.demo.global.api.ApiResponse;
import com.prography.demo.repository.RoomRepository;
import com.prography.demo.repository.UserRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GameService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final TaskScheduler taskScheduler;

    @Transactional
    public ApiResponse<Void> startGame(Integer roomId, Integer userId) {
        // 1) 존재 여부
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponse.onFailure(null);
        }

        // 2) host인가?
        if (!room.getHost().equals(userId)) {
            return ApiResponse.onFailure(null);
        }

        // 3) 대기상태인지
        if (room.getStatus() != RoomStatus.WAIT) {
            return ApiResponse.onFailure(null);
        }

        // 4) 정원(단식=2, 복식=4) 꽉 찼는지
        long participantCount = userRoomRepository.countByRoomId(roomId);
        long capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        if (participantCount < capacity) {
            return ApiResponse.onFailure(null);
        }

        // 5) 상태를 PROGRESS로 변경
        room.setStatus(RoomStatus.PROGRESS);
        roomRepository.save(room);

        // 6) 1분 뒤 FINISH
        scheduleRoomFinish(roomId, Duration.ofMinutes(1));

        return ApiResponse.onSuccess(null);
    }

    private void scheduleRoomFinish(Integer roomId, Duration delay) {
        // delay 후에 방 상태 FINISH로 바꾸기
        taskScheduler.schedule(() -> {
            Room r = roomRepository.findById(roomId).orElse(null);
            if (r != null && r.getStatus() == RoomStatus.PROGRESS) {
                r.setStatus(RoomStatus.FINISH);
                roomRepository.save(r);
            }
        }, Instant.now().plus(delay));
    }

    @Transactional
    public ApiResponse<Void> changeTeam(Integer roomId, Integer userId) {
        // 1) Room 존재 여부
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponse.onFailure(null);
        }

        // 2) 방 상태가 WAIT인지
        if (room.getStatus() != RoomStatus.WAIT) {
            return ApiResponse.onFailure(null);
        }

        // 3) userId가 해당 방에 참가 중인지
        UserRoom userRoom = userRoomRepository.findByUsersIdAndRoomId(userId, roomId).orElse(null);
        if (userRoom == null) {
            return ApiResponse.onFailure(null);
        }

        // 4) 반대 팀 인원 수 체크
        // 만약 현재 팀이 RED라면 BLUE 팀 인원을 확인해야 함.
        TeamType currentTeam = userRoom.getTeam();
        TeamType oppositeTeam = (currentTeam == TeamType.RED) ? TeamType.BLUE : TeamType.RED;

        long capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        long halfCapacity = capacity / 2;  // SINGLE=1, DOUBLE=2

        Integer oppositeTeamCount = userRoomRepository.countByRoomIdAndTeam(roomId, oppositeTeam);
        // 이미 반대팀 인원이 halfCapacity면 변경 불가
        if (oppositeTeamCount >= halfCapacity) {
            return ApiResponse.onFailure(null);
        }

        // 5) 팀 변경
        userRoom.setTeam(oppositeTeam);

        return ApiResponse.onSuccess(null);
    }
}