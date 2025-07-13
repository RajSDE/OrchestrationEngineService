-- Database initialization script

-- Create the main database
CREATE DATABASE maindb;

-- Create an application user
CREATE USER orchestration_engine WITH ENCRYPTED PASSWORD 'Raj@1357';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE maindb TO orchestration_engine;

-- Connect to the main database
\c maindb

-- Create a schema and set permissions
CREATE SCHEMA IF NOT EXISTS maindb AUTHORIZATION orchestration_engine;
ALTER ROLE orchestration_engine SET search_path TO maindb;
