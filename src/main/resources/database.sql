DROP TABLE IF EXISTS ocr;
DROP TABLE IF EXISTS search_history;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS area;
DROP TABLE IF EXISTS propery;
DROP TABLE IF EXISTS player_property;

DROP TABLE IF EXISTS lock;
DROP TABLE IF EXISTS unit;
DROP TABLE IF EXISTS player;

CREATE TABLE player (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    profile_folder VARCHAR(255) NOT NULL,

    priority INT NOT NULL,
    
    lock BOOLEAN NOT NULL DEFAULT false,
    halt BOOLEAN NOT NULL DEFAULT false,

    lumber BIGINT DEFAULT 0 NOT NULL,
    iron BIGINT DEFAULT 0 NOT NULL,
    stone BIGINT DEFAULT 0 NOT NULL,
    silver BIGINT DEFAULT 0 NOT NULL,
    common_tar BIGINT DEFAULT 0 NOT NULL,

    target_lumber BIGINT DEFAULT 0 NOT NULL,
    target_iron BIGINT DEFAULT 0 NOT NULL,
    target_stone BIGINT DEFAULT 0 NOT NULL,
    target_silver BIGINT DEFAULT 0 NOT NULL,

    last_logout TIMESTAMP,
    playing BOOLEAN,
    version TIMESTAMP,

    leadership BIGINT DEFAULT 0 NOT NULL,
    dominance BIGINT DEFAULT 0 NOT NULL,
    authority BIGINT DEFAULT 0 NOT NULL,
    attack_waves INT DEFAULT 0 NOT NULL,

    has_helen BOOLEAN NOT NULL DEFAULT false,

    position_x INT DEFAULT 0 NOT NULL,
    position_y INT DEFAULT 0 NOT NULL,

    common_crypt_level INT DEFAULT 0 NOT NULL,
    rare_crypt_level INT DEFAULT 0 NOT NULL,

    common_tar_required BIGINT DEFAULT 0 NOT NULL,
    rare_tar_required BIGINT DEFAULT 0 NOT NULL,
    
    common_crypt_exploring_location JSONB,

    citadel_level INT DEFAULT 0 NOT NULL,

    kingdom INT NOT NULL,
    clan VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX uq_player_name_ci
    ON player (LOWER(name));

CREATE INDEX idx_player_player_name
    ON player(name);

CREATE TABLE unit (
  id BIGSERIAL PRIMARY KEY,

  player_id BIGINT NOT NULL,

  name VARCHAR(255) NOT NULL,
  priority INT NOT NULL,

  CONSTRAINT fk_unit_player
      FOREIGN KEY (player_id)
          REFERENCES player(id)
);
    
CREATE TABLE lock (
    id BIGSERIAL PRIMARY KEY,
    
    player_id BIGINT NOT NULL,

    scenario VARCHAR(255) NOT NULL,
    
    CONSTRAINT fk_lock_player
      FOREIGN KEY (player_id)
          REFERENCES player(id)
);

CREATE TABLE property (
      name VARCHAR(255) PRIMARY KEY,
      value VARCHAR(255) NOT NULL
);

CREATE TABLE player_property (
    player_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    PRIMARY KEY (player_id, name),
    CONSTRAINT fk_player_property_player
        FOREIGN KEY (player_id)
            REFERENCES player(id)
);

CREATE TABLE location (
     id BIGSERIAL PRIMARY KEY,
     location JSONB NOT NULL
);

CREATE TABLE area (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      x INT NOT NULL,
      y INT NOT NULL,
      width INT NOT NULL,
      height INT NOT NULL
);
    
CREATE UNIQUE INDEX uq_area
    ON area(name);


CREATE TABLE search_history (
    id BIGSERIAL PRIMARY KEY,

    screen BIGINT NOT NULL,
    item BIGINT NOT NULL,
    
    limit DOUBLE PRECISION NOT NULL,

    x INT NOT NULL,
    y INT NOT NULL,
    hits BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_search_history
    ON search_history(screen, item);

CREATE TABLE ocr (
        id BIGSERIAL PRIMARY KEY,
        crc BIGINT NOT NULL,
        data BYTEA,
        value VARCHAR(255),
        hits BIGINT NOT NULL DEFAULT 0,
        errors BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_ocr
    ON ocr(crc);

