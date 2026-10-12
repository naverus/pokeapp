CREATE TABLE users
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    username      VARCHAR(30) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20) NOT NULL,
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE pokemon
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    pokeapi_id  INT           NULL,
    name        VARCHAR(100)  NOT NULL,
    local_name  VARCHAR(100)  NULL,
    region      VARCHAR(50)   NULL,
    category    VARCHAR(100)  NULL,
    height_m    DOUBLE        NOT NULL,
    weight_kg   DOUBLE        NOT NULL,
    sprite_url  VARCHAR(500)  NULL,
    description VARCHAR(1000) NULL,
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_pokemon PRIMARY KEY (id),
    CONSTRAINT uk_pokemon_pokeapi_id UNIQUE (pokeapi_id)
);

CREATE INDEX ix_pokemon_name ON pokemon (name);

CREATE TABLE pokemon_type
(
    pokemon_id BIGINT      NOT NULL,
    position   INT         NOT NULL,
    type_name  VARCHAR(30) NOT NULL,
    CONSTRAINT pk_pokemon_type PRIMARY KEY (pokemon_id, position),
    CONSTRAINT fk_pokemon_type_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon (id) ON DELETE CASCADE
);

CREATE TABLE pokemon_ability
(
    pokemon_id   BIGINT      NOT NULL,
    position     INT         NOT NULL,
    ability_name VARCHAR(50) NOT NULL,
    CONSTRAINT pk_pokemon_ability PRIMARY KEY (pokemon_id, position),
    CONSTRAINT fk_pokemon_ability_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon (id) ON DELETE CASCADE
);

CREATE TABLE pokemon_tag
(
    pokemon_id BIGINT      NOT NULL,
    position   INT         NOT NULL,
    tag        VARCHAR(30) NOT NULL,
    CONSTRAINT pk_pokemon_tag PRIMARY KEY (pokemon_id, position),
    CONSTRAINT fk_pokemon_tag_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon (id) ON DELETE CASCADE
);
