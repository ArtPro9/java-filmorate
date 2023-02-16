package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(createId());
        films.put(film.getId(), film);
        likes.put(film.getId(), new LinkedHashSet<>());
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(Film film) {
        likes.remove(film.getId());
        films.remove(film.getId());
    }

    @Override
    public Film getFilm(long filmId) {
        return films.get(filmId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        likes.getOrDefault(filmId, new LinkedHashSet<>()).add(userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        likes.getOrDefault(filmId, new LinkedHashSet<>()).remove(userId);
    }

    @Override
    public Map<Long, Set<Long>> getLikes() {
        return likes;
    }

    private long createId() {
        return (long) films.values().size() + 1;
    }
}
