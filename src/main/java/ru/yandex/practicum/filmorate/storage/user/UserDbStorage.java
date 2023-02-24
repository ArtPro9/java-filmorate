package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static ru.yandex.practicum.filmorate.model.Friendship.APPROVED;
import static ru.yandex.practicum.filmorate.model.Friendship.UNAPPROVED;
import static ru.yandex.practicum.filmorate.storage.StorageUtils.convertFromOptionalList;

@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> getAll() {
        String sqlQuery = "SELECT * FROM USERS " +
                "ORDER BY \"user_id\"";
        return convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractUser(rs)));
    }

    private Optional<User> extractUser(ResultSet rs) throws SQLException {
        long userId = rs.getLong("user_id");
        if (userId < 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(User.builder()
                .id(userId)
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(Optional.ofNullable(rs.getDate("birthday")).map(Date::toLocalDate).orElse(null))
                .build());
    }

    @Override
    public User create(User user) {
        if (isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        final String sqlQuery = "INSERT INTO USERS (\"email\", \"login\", \"name\", \"birthday\") " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        long userId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return getUser(userId);
    }

    @Override
    public User update(User user) {
        if (isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        String sqlQuery = "UPDATE USERS " +
                "SET \"email\" = ?, \"login\" = ?, \"name\" = ?, \"birthday\" = ? " +
                "WHERE \"user_id\" = ?";
        jdbcTemplate.update(sqlQuery, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return getUser(user.getId());
    }

    @Override
    public void delete(User user) {
        String sqlQuery = "DELETE FROM USERS WHERE \"user_id\" = ?";
        jdbcTemplate.update(sqlQuery, user.getId());

        sqlQuery = "DELETE FROM USER_FRIENDS WHERE \"user_id\" = ?";
        jdbcTemplate.update(sqlQuery, user.getId());

        sqlQuery = "DELETE FROM FRIENDSHIPS WHERE \"friend_id\" = ?";
        jdbcTemplate.update(sqlQuery, user.getId());

        sqlQuery = "DELETE FROM LIKES WHERE \"user_id\" = ?";
        jdbcTemplate.update(sqlQuery, user.getId());
    }

    @Override
    public User getUser(long userId) {
        String sqlQuery = "SELECT * FROM USERS WHERE \"user_id\" = ?";
        List<User> users = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractUser(rs), userId));
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        Optional<Friendship> directFriendship = getFriendship(userId, friendId);
        if (directFriendship.isPresent()) {
            return;
        }
        Optional<Friendship> oppositeFriendship = getFriendship(friendId, userId);
        String status = oppositeFriendship.isPresent() ? APPROVED : UNAPPROVED;
        oppositeFriendship.ifPresent(friendship -> updateFriendshipStatus(friendship, status));
        Friendship friendship = createFriendship(userId, friendId, status);
        String sqlQuery = "INSERT INTO USER_FRIENDS (\"user_id\", \"friendship_id\") VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, userId, friendship.getId());
    }

    private Friendship createFriendship(long userId, long friendId, String status) {
        final String sqlQuery = "INSERT INTO FRIENDSHIPS (\"user_id\", \"friend_id\", \"status\") VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"friendship_id"});
            ps.setLong(1, userId);
            ps.setLong(2, friendId);
            ps.setString(3, status);
            return ps;
        }, keyHolder);
        return Friendship.builder()
                .id(Objects.requireNonNull(keyHolder.getKey()).longValue())
                .userId(userId)
                .friendId(friendId)
                .status(status)
                .build();
    }

    private Optional<Friendship> getFriendship(long userId, long friendId) {
        String sqlQuery = "SELECT * FROM FRIENDSHIPS " +
                "WHERE \"user_id\" = ? AND \"friend_id\" = ?";
        List<Friendship> friendships = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractFriendship(rs), userId, friendId));
        if (friendships.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(friendships.get(0));
    }

    private Optional<Friendship> extractFriendship(ResultSet rs) throws SQLException {
        long friendshipId = rs.getLong("friendship_id");
        if (friendshipId < 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(Friendship.builder()
                .id(friendshipId)
                .userId(rs.getLong("user_id"))
                .friendId(rs.getLong("friend_id"))
                .status(rs.getString("status"))
                .build());
    }

    private void updateFriendshipStatus(Friendship friendship, String status) {
        String sqlQuery = "UPDATE FRIENDSHIPS SET \"status\" = ? WHERE \"friendship_id\" = ?";
        jdbcTemplate.update(sqlQuery, status, friendship.getId());
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        Optional<Friendship> directFriendship = getFriendship(userId, friendId);
        if (directFriendship.isEmpty()) {
            return;
        }
        Optional<Friendship> oppositeFriendship = getFriendship(friendId, userId);
        oppositeFriendship.ifPresent(friendship -> updateFriendshipStatus(friendship, UNAPPROVED));
        String sqlQuery = "DELETE FROM FRIENDSHIPS WHERE \"friendship_id\" = ? ";
        jdbcTemplate.update(sqlQuery, directFriendship.get().getId());

        sqlQuery = "DELETE FROM USER_FRIENDS WHERE \"friendship_id\" = ?";
        jdbcTemplate.update(sqlQuery, directFriendship.get().getId());
    }

    @Override
    public Set<User> getUserFriends(long userId) {
        String sqlQuery = "SELECT U2.* FROM USERS U " +
                "LEFT JOIN USER_FRIENDS UF on U.\"user_id\" = UF.\"user_id\" " +
                "LEFT JOIN FRIENDSHIPS F on UF.\"friendship_id\" = F.\"friendship_id\" " +
                "LEFT JOIN USERS U2 on U2.\"user_id\" = F.\"friend_id\" " +
                "WHERE U.\"user_id\" = ?" +
                "ORDER BY U2.\"user_id\"";
        List<User> users = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractUser(rs), userId));
        if (users.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(users);
    }
}
