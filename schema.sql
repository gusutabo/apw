CREATE DATABASE IF NOT EXISTS apw
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE apw;

CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    details TEXT,
    done BOOLEAN NOT NULL DEFAULT FALSE,
    category VARCHAR(50) NOT NULL DEFAULT 'None',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subtasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    done BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_subtask_task
        FOREIGN KEY (task_id) REFERENCES tasks(id)
        ON DELETE CASCADE
);

-- Para bancos criados com uma versão anterior do APW:
-- ALTER TABLE tasks ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT 'None';
