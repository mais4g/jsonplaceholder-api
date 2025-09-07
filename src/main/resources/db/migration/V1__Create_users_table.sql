-- V1__Create_users_table.sql
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL,
                       phone VARCHAR(20),
                       website VARCHAR(100),

    -- Address fields (embedded)
                       street VARCHAR(100),
                       suite VARCHAR(50),
                       city VARCHAR(50),
                       zipcode VARCHAR(20),
                       lat VARCHAR(20),
                       lng VARCHAR(20),

    -- Company fields (embedded)
                       company_name VARCHAR(100),
                       company_catch_phrase VARCHAR(200),
                       company_bs VARCHAR(200),

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);