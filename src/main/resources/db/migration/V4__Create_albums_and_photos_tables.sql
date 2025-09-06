-- V4__Create_albums_and_photos_tables.sql

-- Albums table
CREATE TABLE IF NOT EXISTS albums (
                                      id BIGSERIAL PRIMARY KEY,
                                      title VARCHAR(200) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Photos table
CREATE TABLE IF NOT EXISTS photos (
                                      id BIGSERIAL PRIMARY KEY,
                                      title VARCHAR(200) NOT NULL,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(100),
    album_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
    );

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_albums_user_id ON albums(user_id);
CREATE INDEX IF NOT EXISTS idx_albums_created_at ON albums(created_at);
CREATE INDEX IF NOT EXISTS idx_photos_album_id ON photos(album_id);
CREATE INDEX IF NOT EXISTS idx_photos_created_at ON photos(created_at);