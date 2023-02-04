package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    public static void validate(User user) throws ValidationException {
        if (isBlank(user.getEmail())) {
            throw new ValidationException("Input error: 'email' is empty!");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Input error: 'email' must contain '@'!");
        }
        if (isBlank(user.getLogin())) {
            throw new ValidationException("Input error: 'login' is empty!");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Input error: 'login' must not contain blanks!");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Input error: 'birthday' must not be in future!");
        }
    }

    private int createId() {
        return users.values().size() + 1;
    }

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException {
        log.info("User validation started");
        try {
            validate(user);
        } catch (ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
        log.info("User validation successful");
        if (isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        user.setId(createId());
        users.put(user.getId(), user);
        log.info("User created");
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) throws ValidationException {
        log.info("User validation started");
        try {
            if (!users.containsKey(user.getId())) {
                throw new ValidationException("Input error: 'id' not found");
            }
            validate(user);
        } catch (ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
        log.info("User validation successful");
        if (isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("User updated");
        return user;
    }
}
