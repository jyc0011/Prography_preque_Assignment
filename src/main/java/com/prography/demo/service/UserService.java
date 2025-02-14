package com.prography.demo.service;

import com.prography.demo.domain.User;
import com.prography.demo.dto.response.UserResponseDto;
import com.prography.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // 유저 전체 조회
    public Page<UserResponseDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<User> userPage = userRepository.findAll(pageable);

        return userPage.map(UserResponseDto::fromEntity);
    }
}
