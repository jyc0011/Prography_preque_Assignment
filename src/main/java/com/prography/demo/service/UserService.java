package com.prography.demo.service;

import com.prography.demo.domain.Users;
import com.prography.demo.dto.response.UserResponseDto;
import com.prography.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Page<UserResponseDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Users> userPage = userRepository.findAll(pageable);
        return userPage.map(UserResponseDto::fromEntity);
    }
}
