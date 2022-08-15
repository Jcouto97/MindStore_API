package com.example.persistence.model.user;

import MindStore.command.personDto.UserDto;
import MindStore.persistence.models.Person.User;

import java.time.LocalDate;

public class UserPojo {
    public static final User USER_EXAMPLE = User.builder()
            .id(1L)
            .firstName("joao")
            .lastName("couto")
            .email("joao@hotmail.com")
            .password("123456789")
            .dateOfBirth(LocalDate.parse("1800-12-12"))
            .address("rua do carmo")
            .build();

    public static final UserDto USER_DTO_EXAMPLE = UserDto.builder()
            .id(1L)
            .firstName("joao")
            .lastName("couto")
            .email("joao@hotmail.com")
            .password("123456789")
            .dateOfBirth(LocalDate.parse("1800-12-12"))
            .address("rua do carmo")
            .build();
}
