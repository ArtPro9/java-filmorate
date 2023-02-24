package ru.yandex.practicum.filmorate.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StorageUtils {
    public static <T, C extends Collection<T>> Collection<T> convertFromOptional(Collection<Optional<T>> collection, Supplier<C> collector) {
        return collection.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(collector));
    }

    public static <T, C extends List<T>> List<T> convertFromOptionalList(Collection<Optional<T>> collection) {
        return (List<T>) convertFromOptional(collection, ArrayList::new);
    }
}
