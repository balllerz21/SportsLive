package org.example.sportslivev1.dto;

import java.util.List;

import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.dto.AlertMapper;
import org.example.sportslivev1.dto.AlertResponse;

public class UsersMapper {
    public static UserResponse toUserResponse(Users user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUserName());
        response.setRole(user.getRole());
        List<AlertResponse> alerts = user.getAlerts() == null
            ? List.of()
            : user.getAlerts().stream().map(AlertMapper::toResponse).toList();
        response.setAlerts(alerts);
        return response;
    }
    public static Users toUserEntity(UserRequest request) {
        Users user = new Users(request.getUsername(), request.getPasswordHash(), request.getRole());
        return user;
    }
}
