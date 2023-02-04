package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.filmorate.controller.FilmController.validate;

public class FilmValidationTest {
    @Test
    void testValid() {
        Film film = Film.builder().name("test").description("test").releaseDate(LocalDate.of(2000, 1, 1)).duration(1).build();
        assertDoesNotThrow(() -> validate(film));
    }

    @Test
    void testEmptyName() {
        Film film = Film.builder().build();
        assertThrows(ValidationException.class, () -> validate(film));
    }

    @Test
    void testDescriptionLength200() {
        Film film = Film.builder().name("test").description("a".repeat(200)).duration(1).build();
        assertDoesNotThrow(() -> validate(film));
    }

    @Test
    void testDescriptionLength201() {
        Film film = Film.builder().name("test").description("a".repeat(201)).duration(1).build();
        assertThrows(ValidationException.class, () -> validate(film));
    }

    @Test
    void testReleaseDateEqualsBirthday() {
        Film film = Film.builder().name("test").releaseDate(LocalDate.of(1895, 12, 28)).duration(1).build();
        assertDoesNotThrow(() -> validate(film));
    }

    @Test
    void testReleaseDateBeforeBirthday() {
        Film film = Film.builder().name("test").releaseDate(LocalDate.of(1895, 12, 27)).duration(1).build();
        assertThrows(ValidationException.class, () -> validate(film));
    }

    @Test
    void testDuration0() {
        Film film = Film.builder().name("test").duration(0).build();
        assertThrows(ValidationException.class, () -> validate(film));
    }

    @Test
    void testNegativeDuration() {
        Film film = Film.builder().name("test").duration(-1).build();
        assertThrows(ValidationException.class, () -> validate(film));
    }
}
