package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static ru.yandex.practicum.filmorate.storage.StorageUtils.convertFromOptionalList;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> getAll() {
        String sqlQuery = "SELECT * FROM FILMS " +
                "ORDER BY \"film_id\"";
        return convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractFilm(rs)));
    }

    private Optional<Film> extractFilm(ResultSet rs) throws SQLException {
        long filmId = rs.getLong("film_id");
        if (filmId < 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(Film.builder()
                .id(filmId)
                .name(rs.getString("title"))
                .description(rs.getString("description"))
                .releaseDate(Optional.ofNullable(rs.getDate("release_date")).map(Date::toLocalDate).orElse(null))
                .duration(rs.getInt("duration"))
                .genres(getFilmGenres(filmId))
                .mpa(getMpa(rs.getLong("rating_id")))
                .build());
    }

    private Optional<Mpa> extractMpa(ResultSet rs) throws SQLException {
        long mpaId = rs.getLong("rating_id");
        if (mpaId < 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(Mpa.builder()
                .id(mpaId)
                .name(rs.getString("name"))
                .build());
    }

    private Set<Genre> getFilmGenres(long filmId) {
        String sqlQuery = "SELECT G.\"genre_id\", G.\"name\" FROM FILM_GENRE FG " +
                "LEFT JOIN GENRES G on FG.\"genre_id\" = G.\"genre_id\"" +
                "WHERE FG.\"film_id\" = ?" +
                "ORDER BY G.\"genre_id\"";
        List<Genre> genres = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractGenre(rs), filmId));
        if (genres.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(genres);
    }

    private Optional<Genre> extractGenre(ResultSet rs) throws SQLException {
        long genreId = rs.getLong("genre_id");
        if (genreId < 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(Genre.builder()
                .id(genreId)
                .name(rs.getString("name"))
                .build());
    }

    @Override
    public Collection<Genre> getAllGenres() {
        String sqlQuery = "SELECT * FROM GENRES G " +
                "ORDER BY G.\"genre_id\"";
        List<Genre> genres = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractGenre(rs)));
        if (genres.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(genres);
    }

    @Override
    public Genre getGenre(long genreId) {
        String sqlQuery = "SELECT * FROM GENRES G WHERE G.\"genre_id\" = ?";
        List<Genre> genres = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractGenre(rs), genreId));
        if (genres.isEmpty()) {
            return null;
        }
        return genres.get(0);
    }

    @Override
    public Collection<Mpa> getAllMpa() {
        String sqlQuery = "SELECT * FROM MPA_RATINGS M " +
                "ORDER BY M.\"rating_id\"";
        List<Mpa> mpas = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractMpa(rs)));
        if (mpas.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(mpas);
    }

    @Override
    public Mpa getMpa(long mpaId) {
        String sqlQuery = "SELECT * FROM MPA_RATINGS M WHERE M.\"rating_id\" = ?";
        List<Mpa> mpas = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractMpa(rs), mpaId));
        if (mpas.isEmpty()) {
            return null;
        }
        return mpas.get(0);
    }

    @Override
    public Film create(Film film) {
        final String sqlQuery =
                "INSERT INTO FILMS (\"title\", \"description\", \"release_date\", \"duration\", \"rating_id\") " +
                        "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        for (Genre genre : Optional.ofNullable(film.getGenres()).orElse(new LinkedHashSet<>())) {
            String sqlQueryGenre = "INSERT INTO FILM_GENRE (\"film_id\", \"genre_id\") VALUES (?, ?)";
            jdbcTemplate.update(sqlQueryGenre, filmId, genre.getId());
        }
        return getFilm(filmId);
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE FILMS " +
                "SET \"title\" = ?, \"description\" = ?, \"release_date\" = ?, \"duration\" = ?, \"rating_id\" = ? " +
                "WHERE \"film_id\" = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        updateGenres(film.getId(), film.getGenres());
        return getFilm(film.getId());
    }

    private void updateGenres(long filmId, Set<Genre> genres) {
        String sqlQuery = "DELETE FROM FILM_GENRE WHERE \"film_id\" = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        for (Genre genre : Optional.ofNullable(genres).orElse(new LinkedHashSet<>())) {
            String sqlQueryGenre = "INSERT INTO FILM_GENRE (\"film_id\", \"genre_id\") VALUES (?, ?)";
            jdbcTemplate.update(sqlQueryGenre, filmId, genre.getId());
        }
    }

    @Override
    public void delete(Film film) {
        String sqlQuery = "DELETE FROM FILMS WHERE \"film_id\" = ?";
        jdbcTemplate.update(sqlQuery, film.getId());

        sqlQuery = "DELETE FROM FILM_GENRE WHERE \"film_id\" = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
    }

    @Override
    public Film getFilm(long filmId) {
        String sqlQuery = "SELECT * FROM FILMS F " +
                "WHERE \"film_id\" = ? " +
                "ORDER BY F.\"film_id\"";
        List<Film> films = convertFromOptionalList(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractFilm(rs), filmId));
        if (films.isEmpty()) {
            return null;
        }
        return films.get(0);
    }

    @Override
    public void addLike(long filmId, long userId) {
        String sqlQuery = "INSERT INTO LIKES (\"film_id\", \"user_id\") VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        String sqlQuery = "DELETE FROM LIKES WHERE \"film_id\" = ? AND \"user_id\" = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public Map<Long, Set<Long>> getLikes() {
        String sqlQuery = "SELECT * FROM FILMS F " +
                "LEFT JOIN LIKES L on F.\"film_id\" = L.\"film_id\" " +
                "ORDER BY L.\"like_id\"";
        Map<Long, Set<Long>> likes = new LinkedHashMap<>();
        jdbcTemplate.query(sqlQuery, (rs, rowNum) -> extractLike(rs)).forEach(optionalLike -> {
            if (optionalLike.isEmpty()) {
                return;
            }
            Like like = optionalLike.get();
            Set<Long> filmLikes = likes.getOrDefault(like.getFilmId(), new LinkedHashSet<>());
            if (like.getUserId() > 0) {
                filmLikes.add(like.getUserId());
            }
            likes.put(like.getFilmId(), filmLikes);
        });
        return likes;
    }

    private Optional<Like> extractLike(ResultSet rs) throws SQLException {
        long filmId = rs.getLong("film_id");
        if (filmId < 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(Like.builder()
                .id(rs.getLong("like_id"))
                .filmId(filmId)
                .userId(rs.getLong("user_id"))
                .build());
    }
}
