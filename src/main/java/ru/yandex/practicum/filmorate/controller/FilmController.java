package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final Map<Integer, Film> films = new HashMap<>();

    public static void validate(Film film) throws ValidationException {
        if (isBlank(film.getName())) {
            throw new ValidationException("Input error: 'name' is empty!");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Input error: 'description' is longer than 200 characters!");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FILM_BIRTHDAY)) {
            throw new ValidationException("Input error: 'releaseDate' is before than " + FILM_BIRTHDAY.format(ISO_LOCAL_DATE) + "!");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Input error: 'duration' must be positive!");
        }
    }

    private int createId() {
        return films.values().size() + 1;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Film validation started");
        try {
            validate(film);
        } catch (ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
        log.info("Film validation successful");
        film.setId(createId());
        films.put(film.getId(), film);
        log.info("Film created");
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Film validation started");
        try {
            if (!films.containsKey(film.getId())) {
                throw new ValidationException("Input error: 'id' not found");
            }
            validate(film);
        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw e;
        }
        log.info("Film validation successful");
        films.put(film.getId(), film);
        log.info("Film updated");
        return film;
    }
}
