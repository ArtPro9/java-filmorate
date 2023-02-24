package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface FilmService {
    Collection<Film> getAll();

    Film getFilmById(long filmId);

    Film create(Film film);

    Film update(Film film);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    Collection<Film> getTopFilms(int count);

    Collection<Genre> getAllGenres();

    Genre getGenreById(long genreId);

    Collection<Mpa> getAllMpa();

    Mpa getMpaById(long mpaId);

    void checkFilmExisting(long filmId);
}