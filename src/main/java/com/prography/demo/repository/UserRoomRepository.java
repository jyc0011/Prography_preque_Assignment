package com.prography.demo.repository;

import com.prography.demo.domain.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoomRepository extends JpaRepository<UserRoom, Integer> {
    // user가 현재 어떤 방에 참여중인지
    Optional<UserRoom> findByUser_id(Integer user_id);

    // 특정 방에 참여중인 사람 수
    Integer countByRoom_id(Integer room_id);

    // 특정 유저가 특정 방에 참가중인지
    Optional<UserRoom> findByUser_idAndRoom_id(Integer user_id, Integer room_id);

    // 특정 방에 참가중인 모든 UserRoom 목록
    List<UserRoom> findAllByRoom_id(Integer room_id);
}