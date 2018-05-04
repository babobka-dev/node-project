package ru.babobka.nodebusiness.service;

import ru.babobka.nodebusiness.dto.UserDTO;
import ru.babobka.nodebusiness.model.User;

import java.util.List;
import java.util.UUID;


public interface NodeUsersService {

    List<User> getList();

    User get(UUID id);

    User get(String login);

    boolean remove(UUID id);

    void add(UserDTO user);

    boolean update(UUID id, UserDTO user);

    void createDebugUser();
}
