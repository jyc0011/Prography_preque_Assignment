package com.prography.demo.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FakerUsersResponse {
    private String status;
    private int code;
    private int total;
    private List<FakerUserData> data;

    // uuid, firstname, lastname, password, ip, macAddress, website, image 필드는 사용X
    @Getter
    @NoArgsConstructor
    public static class FakerUserData {
        private int id;
        private String username;
        private String email;
    }
}