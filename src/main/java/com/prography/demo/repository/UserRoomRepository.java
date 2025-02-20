package com.prography.demo.repository;

import com.prography.demo.domain.UserRoom;
import com.prography.demo.domain.enumType.TeamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, Integer> {
    // user가 현재 어떤 방에 참여중인지
    Optional<UserRoom> findByUsersId(Integer user_id);

    // 특정 방에 참여중인 사람 수
    Integer countByRoomId(Integer room_id);

    // 특정 유저가 특정 방에 참가중인지
    Optional<UserRoom> findByUsersIdAndRoomId(Integer user_id, Integer room_id);

    // 특정 방에 참가중인 모든 UserRoom 목록
    List<UserRoom> findAllByRoomId(Integer room_id);

    Integer countByRoomIdAndTeam(Integer roomId, TeamType oppositeTeam);
}