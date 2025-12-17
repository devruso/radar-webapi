-- Script para criar banco e usuário (executar com superuser)
-- Uso: psql -h <host> -U <superuser> -f sql/create_user_and_db.sql

-- Ajuste os nomes/senhas abaixo conforme necessário
CREATE DATABASE radar;
CREATE USER radar_user WITH PASSWORD 'radar_pass';
GRANT ALL PRIVILEGES ON DATABASE radar TO radar_user;
