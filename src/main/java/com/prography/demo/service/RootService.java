package com.prography.demo.service;

import com.prography.demo.domain.Users;
import com.prography.demo.domain.enumType.UserStatus;
import com.prography.demo.dto.response.FakerUsersResponse;
import com.prography.demo.repository.RoomRepository;
import com.prography.demo.repository.UserRepository;
import com.prography.demo.repository.UserRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RootService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final WebClient webClient;

    @Transactional
    public void initDatabase(int seed, int quantity) {
        // 기존에 있던 모든 회원 정보 및 방 정보를 삭제합니다. (즉, 모든 table의 모든 데이터를 삭제합니다.)
        userRoomRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        //  body로 전달받은 seed와 quantity정보를 통해 아래 API를 호출하여 서비스에 필요한 회원 정보를 저장
        String url = String.format("https://fakerapi.it/api/v1/users?_seed=%d&_quantity=%d&_locale=ko_KR", seed, quantity);
        FakerUsersResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(FakerUsersResponse.class)
                .block();

        // 응답 값의 id(fakerId)를 오름차순으로 정렬하여 데이터를 저장합니다.
        List<FakerUsersResponse.FakerUserData> fakerData = response.getData();
        fakerData.sort(Comparator.comparingInt(FakerUsersResponse.FakerUserData::getId));

        for (FakerUsersResponse.FakerUserData data : fakerData) {
            Users users = new Users();
            users.setFakerId(data.getId());     // 응답 값의 id필드는 fakerId로 저장합니다.
            users.setName(data.getUsername());  // username 필드는 name으로 저장합니다.
            users.setEmail(data.getEmail());    // email 필드는 그대로 저장합니다.

            // 회원 상태(status)는 응답 값의 id(fakerId)를 기반하여 아래 규칙에 따라 저장합니다.
            int fid = data.getId();
            // 응답 값의 id(fakerId) 값이 30 이하의 회원은 활성(ACTIVE) 상태로 세팅합니다.
            if (fid <= 30) {
                users.setStatus(UserStatus.ACTIVE);
            } // 응답 값의 id(fakerId) 값이 31 이상, 60 이하의 회원은 대기(WAIT) 상태로 세팅합니다.
            else if (fid <= 60) {
                users.setStatus(UserStatus.WAIT);
            } // 응답 값의 id(fakerId) 값이 61 이상인 회원은 비활성(NON_ACTIVE) 상태로 세팅합니다.
            else {
                users.setStatus(UserStatus.NON_ACTIVE);
            }
            users.setCreated_at(LocalDateTime.now());
            users.setUpdated_at(LocalDateTime.now());
            userRepository.save(users);
            userRepository.flush();
        }
    }
}