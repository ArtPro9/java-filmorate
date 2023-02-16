package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public static void validate(User user) throws ValidationException {
        try {
            if (isBlank(user.getEmail())) {
                throw new ValidationException("'email' is empty!");
            }
            if (!user.getEmail().contains("@")) {
                throw new ValidationException("'email' must contain '@'!");
            }
            if (isBlank(user.getLogin())) {
                throw new ValidationException("'login' is empty!");
            }
            if (user.getLogin().contains(" ")) {
                throw new ValidationException("'login' must not contain blanks!");
            }
            if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("'birthday' must not be in future!");
            }
        } catch (ValidationException e) {
            log.error("Validation error: " + e.getMessage() + " for " + user);
            throw e;
        }
    }

    public void checkUserExisting(long userId) {
        User user = userStorage.getUser(userId);
        if (user == null) {
            log.error("Validation error: User not found with id=" + userId);
            throw new UserNotFoundException("Unknown id=" + userId);
        }
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getUserById(long userId) {
        checkUserExisting(userId);
        return userStorage.getUser(userId);
    }

    public User create(User user) {
        log.info("User validation started: " + user);
        validate(user);
        log.info("User validation successful: " + user);
        user = userStorage.create(user);
        log.info("User created: " + user);
        return user;
    }

    public User update(User user) {
        checkUserExisting(user.getId());
        log.info("User validation started: " + user);
        validate(user);
        log.info("User validation successful: " + user);
        user = userStorage.update(user);
        log.info("User updated: " + user);
        return user;
    }

    public Collection<User> getUserFriends(long userId) {
        checkUserExisting(userId);
        log.info("Getting friends for userId=" + userId);
        return userStorage.getUserFriends(userId);
    }

    public void addFriend(long userId, long friendId) {
        checkUserExisting(userId);
        checkUserExisting(friendId);
        log.info("Adding friend with friendId=" + friendId + " to user with userId=" + userId);
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        checkUserExisting(userId);
        checkUserExisting(friendId);
        log.info("Adding friend with friendId=" + friendId + " from user with userId=" + userId);
        userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getCommonFriends(long userId, long friendId) {
        checkUserExisting(userId);
        checkUserExisting(friendId);
        log.info("Getting common friends for users with userId=" + userId + " and friendId=" + friendId);
        Set<User> userFriends = userStorage.getUserFriends(userId);
        Set<User> otherFriends = userStorage.getUserFriends(friendId);
        userFriends.retainAll(otherFriends);
        return userFriends;
    }
}
