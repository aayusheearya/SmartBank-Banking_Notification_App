package com.bank_notification.service;

import com.bank_notification.dto.UserRegistrationDto;

import com.bank_notification.model.User;


public interface UserService {

    User registerNewUser(UserRegistrationDto dto) throws IllegalArgumentException;

    boolean emailExists(String email);

}