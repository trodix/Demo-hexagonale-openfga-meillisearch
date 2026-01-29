package com.trodix.demo.application.usecase;

import com.trodix.demo.application.model.UserInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUsersUseCase {

    public List<UserInfo> listUsers() {
        // Pour MVP: retourne liste hardcodée des utilisateurs in-memory
        return List.of(
                new UserInfo("admin", "Administrator"),
                new UserInfo("user1", "User 1")
        );
    }
}
