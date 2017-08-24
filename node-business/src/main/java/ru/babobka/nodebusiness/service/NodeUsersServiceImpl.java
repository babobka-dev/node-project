package ru.babobka.nodebusiness.service;


import ru.babobka.nodebusiness.dao.NodeUsersDAO;
import ru.babobka.nodebusiness.dto.UserDTO;
import ru.babobka.nodebusiness.mapper.UserDTOMapper;
import ru.babobka.nodebusiness.model.User;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.util.HashUtil;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NodeUsersServiceImpl implements NodeUsersService {

    private final UserDTOMapper userDTOMapper = Container.getInstance().get(UserDTOMapper.class);

    private final NodeUsersDAO userDAO = Container.getInstance().get(NodeUsersDAO.class);

    @Override
    public List<User> getList() {
        return userDAO.getList();
    }

    @Override
    public User get(UUID id) {
        return userDAO.get(id);
    }

    @Override
    public boolean remove(UUID id) {
        return userDAO.remove(id);
    }

    @Override
    public void add(UserDTO user) {
        userDAO.add(userDTOMapper.map(user));
    }

    @Override
    public boolean update(UUID id, UserDTO user) {
        return userDAO.update(id, userDTOMapper.map(user));
    }

    @Override
    public boolean auth(String login, byte[] hashedPassword) {
        User user = userDAO.get(login);
        return user != null && Arrays.equals(user.getHashedPassword(), hashedPassword);
    }

    @Override
    public void createDebugUser() {
        User user = new User();
        user.setName("test_user");
        user.setEmail("test@email.com");
        user.setId(UUID.randomUUID());
        user.setHashedPassword(HashUtil.sha2("test_password"));
        userDAO.add(user);
    }
}
