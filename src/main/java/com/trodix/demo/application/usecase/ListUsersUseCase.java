package com.trodix.demo.application.usecase;

import com.trodix.demo.adapter.in.dto.UserInfo;
import com.trodix.demo.adapter.in.dto.UserListResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUsersUseCase {

    public UserListResponse listUsers() {
        // Pour MVP: retourne liste hardcodée des utilisateurs in-memory
        List<UserInfo> users = List.of(
                new UserInfo("admin", "Administrator"),
                new UserInfo("user1", "User 1")
        );

        return new UserListResponse(users);
    }
}
