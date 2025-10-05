ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_ID FOREIGN KEY (id) REFERENCES departments (id);

ALTER TABLE departments
    DROP COLUMN employees;

DROP SEQUENCE refresk_tokens_id_seq CASCADE;

ALTER TABLE users
    ALTER COLUMN first_name DROP NOT NULL;

ALTER TABLE users
    ALTER COLUMN last_name DROP NOT NULL;

ALTER TABLE users
    ALTER COLUMN middle_name DROP NOT NULL;

ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;