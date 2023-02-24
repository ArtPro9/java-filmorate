package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Friendship {
    public static final String APPROVED = "Approved";
    public static final String UNAPPROVED = "Unapproved";

    private long id;

    private long userId;

    private long friendId;

    private String status;
}
