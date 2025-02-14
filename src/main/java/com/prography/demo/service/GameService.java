package com.prography.demo.service;

import com.prography.demo.domain.Room;
import com.prography.demo.domain.enumType.RoomStatus;
import com.prography.demo.global.exception.CustomApiException;
import com.prography.demo.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GameService {
    private final RoomRepository roomRepository;
    private final TaskScheduler taskScheduler;

    public void startGame(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomApiException("존재하지 않는 방입니다."));

        // 1) host 여부 확인
        if (!room.getHost().equals(userId)) {
            throw new CustomApiException("호스트가 아닙니다.");
        }
        // 2) 방 상태/정원 체크 (생략)
        // ...

        // 3) 진행중으로 변경
        room.setStatus(RoomStatus.PROGRESS);
        roomRepository.save(room);

        // 4) 1분 후 자동 종료 스케줄링
        scheduleFinish(roomId, Duration.ofMinutes(1));
    }

    private void scheduleFinish(Long roomId, Duration delay) {
        // delay 후에 방 상태를 FINISH로 업데이트
        taskScheduler.schedule(
                () -> {
                    Room r = roomRepository.findById(roomId).orElse(null);
                    if (r != null && r.getStatus() == RoomStatus.PROGRESS) {
                        r.setStatus(RoomStatus.FINISH);
                        roomRepository.save(r);
                    }
                },
                Instant.now().plus(delay)
        );
    }
}