package com.annabelle.backend.dto;

public class UserResponse {
    private Long id;
    private String email;

    public UserResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
/*
* later map user top user response in service or controller
* User user = userRepository.findById(id).orElseThrow();
* UserResponse response = new UserResponse(user.getId(), user.getEmail());
return response;
* */