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

    private int createId() {
        return users.values().size() + 1;
    }

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException {
        log.info("User validation started: " + user);
        validate(user);
        log.info("User validation successful: " + user);
        if (isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        user.setId(createId());
        users.put(user.getId(), user);
        log.info("User created: " + user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) throws ValidationException {
        if (!users.containsKey(user.getId())) {
            log.error("Validation error: 'id' not found for " + user);
            throw new ValidationException("Validation error: 'id' not found for " + user);
        }
        log.info("User validation started: " + user);
        validate(user);
        log.info("User validation successful: " + user);
        if (isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("User updated: " + user);
        return user;
    }
}
