package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    void delete(Film film);

    Film getFilm(long filmId);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    Map<Long, Set<Long>> getLikes();

    Collection<Genre> getAllGenres();

    Genre getGenre(long genreId);

    Collection<Mpa> getAllMpa();

    Mpa getMpa(long mpaId);
}
