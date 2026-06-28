-- Migration to increase token column lengths from 500 to 2000 characters
-- Run this SQL script manually on your PostgreSQL database

ALTER TABLE users ALTER COLUMN gmail_access_token TYPE VARCHAR(2000);
ALTER TABLE users ALTER COLUMN gmail_refresh_token TYPE VARCHAR(2000);
ALTER TABLE users ALTER COLUMN zoom_access_token TYPE VARCHAR(2000);
ALTER TABLE users ALTER COLUMN zoom_refresh_token TYPE VARCHAR(2000);

