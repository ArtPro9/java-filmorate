CREATE TABLE IF NOT EXISTS "FILMS"
(
    "film_id"      long GENERATED BY DEFAULT AS IDENTITY,
    "title"        varchar(100) NOT NULL,
    "description"  varchar(200),
    "release_date" date,
    "duration"     int,
    "rating_id"    long,
    CONSTRAINT "pk_FILMS" PRIMARY KEY ("film_id")
);

CREATE TABLE IF NOT EXISTS "GENRES"
(
    "genre_id" long GENERATED BY DEFAULT AS IDENTITY,
    "name"     varchar(20) NOT NULL,
    CONSTRAINT "pk_GENRES" PRIMARY KEY ("genre_id")
);

CREATE TABLE IF NOT EXISTS "FILM_GENRE"
(
    "id"       long GENERATED BY DEFAULT AS IDENTITY,
    "film_id"  long NOT NULL,
    "genre_id" long NOT NULL,
    CONSTRAINT "pk_FILM_GENRE" PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "MPA_RATINGS"
(
    "rating_id" long GENERATED BY DEFAULT AS IDENTITY,
    "name"      varchar(10) NOT NULL,
    CONSTRAINT "pk_MPA_RATINGS" PRIMARY KEY ("rating_id")
);

CREATE TABLE IF NOT EXISTS "USERS"
(
    "user_id"  long GENERATED BY DEFAULT AS IDENTITY,
    "email"    varchar(100) NOT NULL,
    "login"    varchar(100) NOT NULL,
    "name"     varchar(100) NOT NULL,
    "birthday" date,
    CONSTRAINT "pk_USERS" PRIMARY KEY ("user_id")
);

CREATE TABLE IF NOT EXISTS "FRIENDSHIPS"
(
    "friendship_id" long GENERATED BY DEFAULT AS IDENTITY,
    "user_id"       long        NOT NULL,
    "friend_id"     long        NOT NULL,
    "status"        varchar(20) NOT NULL,
    CONSTRAINT "pk_FRIENDSHIPS" PRIMARY KEY ("friendship_id")
);

CREATE TABLE IF NOT EXISTS "USER_FRIENDS"
(
    "id"            long GENERATED BY DEFAULT AS IDENTITY,
    "user_id"       long NOT NULL,
    "friendship_id" long NOT NULL,
    CONSTRAINT "pk_USER_FRIENDS" PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "LIKES"
(
    "like_id" long GENERATED BY DEFAULT AS IDENTITY,
    "film_id" long NOT NULL,
    "user_id" long NOT NULL,
    CONSTRAINT "pk_LIKES" PRIMARY KEY ("like_id")
);

ALTER TABLE "FILMS"
    ADD CONSTRAINT IF NOT EXISTS "fk_FILMS_rating_id" FOREIGN KEY ("rating_id") REFERENCES "MPA_RATINGS" ("rating_id") ON DELETE CASCADE;

ALTER TABLE "FILM_GENRE"
    ADD CONSTRAINT IF NOT EXISTS "fk_FILM_GENRE_film_id" FOREIGN KEY ("film_id") REFERENCES "FILMS" ("film_id") ON DELETE CASCADE;

ALTER TABLE "FILM_GENRE"
    ADD CONSTRAINT IF NOT EXISTS "fk_FILM_GENRE_genre_id" FOREIGN KEY ("genre_id") REFERENCES "GENRES" ("genre_id") ON DELETE CASCADE;

ALTER TABLE "FRIENDSHIPS"
    ADD CONSTRAINT IF NOT EXISTS "fk_FRIENDSHIPS_user_id" FOREIGN KEY ("user_id") REFERENCES "USERS" ("user_id") ON DELETE CASCADE;

ALTER TABLE "FRIENDSHIPS"
    ADD CONSTRAINT IF NOT EXISTS "fk_FRIENDSHIPS_friend_id" FOREIGN KEY ("friend_id") REFERENCES "USERS" ("user_id") ON DELETE CASCADE;

ALTER TABLE "USER_FRIENDS"
    ADD CONSTRAINT IF NOT EXISTS "fk_USER_FRIENDS_user_id" FOREIGN KEY ("user_id") REFERENCES "USERS" ("user_id") ON DELETE CASCADE;

ALTER TABLE "USER_FRIENDS"
    ADD CONSTRAINT IF NOT EXISTS "fk_USER_FRIENDS_friendship_id" FOREIGN KEY ("friendship_id") REFERENCES "FRIENDSHIPS" ("friendship_id") ON DELETE CASCADE;

ALTER TABLE "LIKES"
    ADD CONSTRAINT IF NOT EXISTS "fk_LIKES_film_id" FOREIGN KEY ("film_id") REFERENCES "FILMS" ("film_id") ON DELETE CASCADE;

ALTER TABLE "LIKES"
    ADD CONSTRAINT IF NOT EXISTS "fk_LIKES_user_id" FOREIGN KEY ("user_id") REFERENCES "USERS" ("user_id") ON DELETE CASCADE;

