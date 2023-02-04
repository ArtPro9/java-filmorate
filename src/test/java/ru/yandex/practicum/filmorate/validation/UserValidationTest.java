package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.filmorate.controller.UserController.validate;

public class UserValidationTest {
    @Test
    void testValid() {
        User user = User.builder().email("a@a.ru").login("test").name("test").birthday(LocalDate.of(2000, 1, 1)).build();
        assertDoesNotThrow(() -> validate(user));
    }

    @Test
    void testEmptyEmail() {
        User user = User.builder().login("test").name("test").build();
        assertThrows(ValidationException.class, () -> validate(user));
    }

    @Test
    void testNotValidEmailFormat() {
        User user = User.builder().email("test").login("test").name("test").build();
        assertThrows(ValidationException.class, () -> validate(user));
    }

    @Test
    void testEmptyLogin() {
        User user = User.builder().email("a@a.ru").name("test").build();
        assertThrows(ValidationException.class, () -> validate(user));
    }

    @Test
    void testLoginWithBlanks() {
        User user = User.builder().email("a@a.ru").login("t e s t").name("test").build();
        assertThrows(ValidationException.class, () -> validate(user));
    }

    @Test
    void testEmptyName() {
        User user = User.builder().email("a@a.ru").login("test").build();
        assertDoesNotThrow(() -> validate(user));
    }

    @Test
    void testBirthdayInFuture() {
        User user = User.builder().email("a@a.ru").login("test").name("test").birthday(LocalDate.of(2100, 1, 1)).build();
        assertThrows(ValidationException.class, () -> validate(user));
    }
}
