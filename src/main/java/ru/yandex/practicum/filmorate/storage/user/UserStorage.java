package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {
    Collection<User> getAll();

    User create(User user);

    User update(User user);

    void delete(User user);

    User getUser(long userId);

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    Set<User> getUserFriends(long userId);
}
