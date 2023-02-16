package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        if (isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        user.setId(createId());
        users.put(user.getId(), user);
        friends.put(user.getId(), new LinkedHashSet<>());
        return user;
    }

    @Override
    public User update(User user) {
        if (isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(User user) {
        long userId = user.getId();
        Set<Long> friendIds = friends.get(userId);
        friendIds.forEach(id -> friends.get(id).remove(userId));
        friends.remove(userId);
        users.remove(userId);
    }

    @Override
    public User getUser(long userId) {
        return users.get(userId);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        friends.getOrDefault(userId, new LinkedHashSet<>()).add(friendId);
        friends.getOrDefault(friendId, new LinkedHashSet<>()).add(userId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        friends.getOrDefault(userId, new LinkedHashSet<>()).remove(friendId);
        friends.getOrDefault(friendId, new LinkedHashSet<>()).remove(userId);
    }

    @Override
    public Set<User> getUserFriends(long userId) {
        Set<Long> friendIds = friends.getOrDefault(userId, new LinkedHashSet<>());
        Set<User> userFriends = new LinkedHashSet<>();
        friendIds.forEach(id -> userFriends.add(getUser(id)));
        return userFriends;
    }

    private long createId() {
        return (long) users.values().size() + 1;
    }
}
