package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public static void validate(Film film) throws ValidationException {
        try {
            if (isBlank(film.getName())) {
                throw new ValidationException("'name' is empty!");
            }
            if (film.getDescription() != null && film.getDescription().length() > 200) {
                throw new ValidationException("'description' is longer than 200 characters!");
            }
            if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FILM_BIRTHDAY)) {
                throw new ValidationException("'releaseDate' is before than " + FILM_BIRTHDAY.format(ISO_LOCAL_DATE) + "!");
            }
            if (film.getDuration() <= 0) {
                throw new ValidationException("'duration' must be positive!");
            }
        } catch (ValidationException e) {
            log.error("Validation error: " + e.getMessage() + " for " + film);
            throw e;
        }
    }

    public void checkFilmExisting(long filmId) {
        Film film = filmStorage.getFilm(filmId);
        if (film == null) {
            log.error("Validation error: Film not found with id=" + filmId);
            throw new FilmNotFoundException("Unknown id=" + filmId);
        }
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilmById(long filmId) {
        checkFilmExisting(filmId);
        return filmStorage.getFilm(filmId);
    }

    public Film create(Film film) {
        log.info("Film validation started: " + film);
        validate(film);
        log.info("Film validation successful: " + film);
        film = filmStorage.create(film);
        log.info("Film created: " + film);
        return film;
    }

    public Film update(Film film) {
        checkFilmExisting(film.getId());
        log.info("Film validation started: " + film);
        FilmService.validate(film);
        log.info("Film validation successful: " + film);
        film = filmStorage.update(film);
        log.info("Film updated: " + film);
        return film;
    }

    public void addLike(long filmId, long userId) {
        checkFilmExisting(filmId);
        userService.checkUserExisting(userId);
        log.info("Adding like for filmId=" + filmId + " and userId=" + userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        checkFilmExisting(filmId);
        userService.checkUserExisting(userId);
        log.info("Deleting like for filmId=" + filmId + " and userId=" + userId);
        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getTopFilms(int count) {
        log.info("Getting top " + count + " films");
        return filmStorage.getLikes()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(e -> e.getValue().size(), Comparator.reverseOrder()))
                .limit(count)
                .map(Map.Entry::getKey)
                .map(this::getFilmById)
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
