package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Test
    public void testCRUDUser() {
        User user = userStorage.create(User.builder()
                .email("a@a.ru")
                .login("test")
                .name("test")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());
        assertNotNull(user);
        assertEquals(1, user.getId());

        user = userStorage.getUser(1);
        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("a@a.ru", user.getEmail());
        assertEquals("test", user.getName());
        assertEquals("test", user.getLogin());

        userStorage.update(User.builder()
                .id(1)
                .email("b@b.ru")
                .login("test")
                .name("test")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());
        user = userStorage.getUser(1);
        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("b@b.ru", user.getEmail());

        userStorage.delete(User.builder()
                .id(1)
                .email("b@b.ru")
                .login("test")
                .name("test")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());
        user = userStorage.getUser(1);
        assertNull(user);
    }

    @Test
    public void testCRUDFilm() {
        Film film = filmStorage.create(Film.builder()
                .name("test")
                .description("test")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1)
                .mpa(Mpa.builder().id(1).build())
                .build());
        assertNotNull(film);
        assertEquals(1, film.getId());

        film = filmStorage.getFilm(1);
        assertNotNull(film);
        assertEquals(1, film.getId());
        assertEquals("test", film.getName());
        assertEquals("test", film.getDescription());
        assertEquals(1, film.getDuration());

        filmStorage.update(Film.builder()
                .id(1)
                .name("test")
                .description("changed")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1)
                .mpa(Mpa.builder().id(1).build())
                .build());
        film = filmStorage.getFilm(1);
        assertNotNull(film);
        assertEquals(1, film.getId());
        assertEquals("changed", film.getDescription());

        filmStorage.delete(Film.builder()
                .id(1)
                .name("test")
                .description("changed")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1)
                .mpa(Mpa.builder().id(1).build())
                .build());
        film = filmStorage.getFilm(1);
        assertNull(film);
    }
} 