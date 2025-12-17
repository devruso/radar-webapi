-- Script de inicialização (opcional)
-- Cria o banco de dados e usuário básicos para desenvolvimento local.
-- Execute no psql como superuser (ex: postgres) se quiser criar o DB automaticamente.

-- Exemplo (no shell):
-- psql -U postgres -c "CREATE DATABASE radar;"
-- psql -U postgres -c "CREATE USER radar_user WITH PASSWORD 'radar_pass';"
-- psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE radar TO radar_user;"

-- Observação: as tabelas são criadas automaticamente pelo Hibernate (spring.jpa.hibernate.ddl-auto=update).

-- Recomenda-se usar o script `populate_data.sql` para inserir dados de exemplo
-- e NÃO executar DDL via esse arquivo quando a aplicação estiver conectada ao
-- mesmo banco (o usuário pode não ter privilégios para criar DB).

-- Para popular dados depois de criar o banco, execute no psql conectado ao
-- banco `radar` como um usuário com privilégios de escrita:
-- \i sql/populate_data.sql
