CREATE TABLE comments
(
    id        CHAR(36)     NOT NULL,
    leaved_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payload   VARCHAR(255) NOT NULL,
    post_id   CHAR(36)     NOT NULL,
    poster_id CHAR(36),
    CONSTRAINT comments_pkey PRIMARY KEY (id)
);

CREATE TABLE images
(
    id               CHAR(36)     NOT NULL,
    description      VARCHAR(255) NOT NULL,
    parent_entity_id CHAR(36)     NOT NULL,
    uploaded_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    url              VARCHAR(255) NOT NULL,
    post_id          CHAR(36),
    CONSTRAINT images_pkey PRIMARY KEY (id)
);

CREATE TABLE likes
(
    id       CHAR(36) NOT NULL,
    liked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    post_id  CHAR(36) NOT NULL,
    user_id  CHAR(36) NOT NULL,
    CONSTRAINT likes_pkey PRIMARY KEY (id)
);

CREATE TABLE posts
(
    id   CHAR(36)     NOT NULL,
    text VARCHAR(255) NOT NULL,
    CONSTRAINT posts_pkey PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         CHAR(36)     NOT NULL,
    avatars    UUID[]       NOT NULL,
    birth_date TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    email      VARCHAR(255) NOT NULL,
    firstname  VARCHAR(255) NOT NULL,
    lastname   VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    username   VARCHAR(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE comments
    ADD CONSTRAINT uk6tpd8p9v0iaw9ukuvh1gk7yj4 UNIQUE (poster_id);

ALTER TABLE users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);

ALTER TABLE images
    ADD CONSTRAINT fkcp0pycisii8ub3q4b7x5mfpn1 FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE NO ACTION;

ALTER TABLE comments
    ADD CONSTRAINT fknkcrm34nisy4i7b9ymvlisyo2 FOREIGN KEY (poster_id) REFERENCES users (id) ON DELETE NO ACTION;
CREATE TABLE comments
(
    id        CHAR(36)                    NOT NULL,
    leaved_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payload   VARCHAR(255)                NOT NULL,
    post_id   CHAR(36)                    NOT NULL,
    poster_id CHAR(36),
    CONSTRAINT comments_pkey PRIMARY KEY (id)
);

CREATE TABLE images
(
    id               CHAR(36)                    NOT NULL,
    description      VARCHAR(255)                NOT NULL,
    parent_entity_id CHAR(36)                    NOT NULL,
    uploaded_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    url              VARCHAR(255)                NOT NULL,
    post_id          CHAR(36),
    CONSTRAINT images_pkey PRIMARY KEY (id)
);

CREATE TABLE likes
(
    id       CHAR(36)                    NOT NULL,
    liked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    post_id  CHAR(36)                    NOT NULL,
    user_id  CHAR(36)                    NOT NULL,
    CONSTRAINT likes_pkey PRIMARY KEY (id)
);

CREATE TABLE posts
(
    id   CHAR(36)     NOT NULL,
    text VARCHAR(255) NOT NULL,
    CONSTRAINT posts_pkey PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         CHAR(36)     NOT NULL,
    avatars    UUID[]       NOT NULL,
    birth_date TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    email      VARCHAR(255) NOT NULL,
    firstname  VARCHAR(255) NOT NULL,
    lastname   VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    username   VARCHAR(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE comments
    ADD CONSTRAINT uk6tpd8p9v0iaw9ukuvh1gk7yj4 UNIQUE (poster_id);

ALTER TABLE users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);

ALTER TABLE images
    ADD CONSTRAINT fkcp0pycisii8ub3q4b7x5mfpn1 FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE NO ACTION;

ALTER TABLE comments
    ADD CONSTRAINT fknkcrm34nisy4i7b9ymvlisyo2 FOREIGN KEY (poster_id) REFERENCES users (id) ON DELETE NO ACTION;