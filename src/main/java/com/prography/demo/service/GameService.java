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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final TaskScheduler taskScheduler;

    @Transactional
    public ApiResponse<Void> startGame(Integer roomId, Integer userId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.error("startGame() 실패: roomId={} 방이 존재하지 않습니다.", roomId);
            return ApiResponse.onFailure(null);
        }
        if (!room.getHost().equals(userId)) {
            log.error("startGame() 실패: 유저(userId={})는 roomId={} 방의 호스트가 아닙니다. hostUserId={}", userId, roomId, room.getHost());
            return ApiResponse.onFailure(null);
        }
        if (room.getStatus() != RoomStatus.WAIT) {
            log.error("startGame() 실패: roomId={} 방 상태가 WAIT가 아님. status={}", roomId, room.getStatus());
            return ApiResponse.onFailure(null);
        }

        long participantCount = userRoomRepository.countByRoomId(roomId);
        long capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        if (participantCount < capacity) {
            log.error("startGame() 실패: roomId={} 정원 부족 participantCount={}/capacity={}", roomId, participantCount, capacity);
            return ApiResponse.onFailure(null);
        }

        room.setStatus(RoomStatus.PROGRESS);
        roomRepository.save(room);
        scheduleRoomFinish(roomId, Duration.ofMinutes(1));

        log.info("startGame() 성공: roomId={}, userId={} 게임 시작 -> 1분 뒤 FINISH 예정", roomId, userId);
        return ApiResponse.onSuccess(null);
    }

    private void scheduleRoomFinish(Integer roomId, Duration delay) {
        taskScheduler.schedule(() -> {
            Room r = roomRepository.findById(roomId).orElse(null);
            if (r != null && r.getStatus() == RoomStatus.PROGRESS) {
                r.setStatus(RoomStatus.FINISH);
                roomRepository.save(r);
                List<UserRoom> participants = userRoomRepository.findAllByRoomId(roomId);
                userRoomRepository.deleteAllInBatch(participants);
                log.info("scheduleRoomFinish() 실행: roomId={} -> FINISH, 모든 유저 퇴장 완료", roomId);
            }
        }, Instant.now().plus(delay));
    }

    @Transactional
    public ApiResponse<Void> changeTeam(Integer roomId, Integer userId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.error("changeTeam() 실패: roomId={} 방이 존재하지 않습니다.", roomId);
            return ApiResponse.onFailure(null);
        }
        if (room.getStatus() != RoomStatus.WAIT) {
            log.error("changeTeam() 실패: roomId={} 방 상태가 WAIT가 아님. status={}", roomId, room.getStatus());
            return ApiResponse.onFailure(null);
        }

        UserRoom userRoom = userRoomRepository.findByUsersIdAndRoomId(userId, roomId).orElse(null);
        if (userRoom == null) {
            log.error("changeTeam() 실패: userId={}은 roomId={} 방에 참가 중이 아님.", userId, roomId);
            return ApiResponse.onFailure(null);
        }

        TeamType currentTeam = userRoom.getTeam();
        TeamType oppositeTeam = (currentTeam == TeamType.RED) ? TeamType.BLUE : TeamType.RED;

        long capacity = (room.getRoom_type() == RoomType.SINGLE) ? 2 : 4;
        long halfCapacity = capacity / 2;

        Integer oppositeTeamCount = userRoomRepository.countByRoomIdAndTeam(roomId, oppositeTeam);
        if (oppositeTeamCount >= halfCapacity) {
            log.error("changeTeam() 실패: roomId={} 반대 팀({}) 인원이 이미 {}명", roomId, oppositeTeam, halfCapacity);
            return ApiResponse.onFailure(null);
        }

        userRoom.setTeam(oppositeTeam);
        log.info("changeTeam() 성공: roomId={}, userId={} {} -> {}", roomId, userId, currentTeam, oppositeTeam);
        return ApiResponse.onSuccess(null);
    }
}