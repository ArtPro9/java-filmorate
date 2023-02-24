package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserService {
    Collection<User> getAll();

    User getUserById(long userId);

    User create(User user);

    User update(User user);

    Collection<User> getUserFriends(long userId);

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    Collection<User> getCommonFriends(long userId, long friendId);

    void checkUserExisting(long userId);
}
