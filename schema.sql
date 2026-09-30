PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT NOT NULL,
    details TEXT,
    done INTEGER NOT NULL DEFAULT 0,
    category TEXT NOT NULL DEFAULT 'None',
    status TEXT NOT NULL DEFAULT 'TODO',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subtasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    description TEXT NOT NULL,
    done INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_subtask_task
        FOREIGN KEY (task_id) REFERENCES tasks(id)
        ON DELETE CASCADE
);

-- Para bancos SQLite criados antes do kanban:
-- ALTER TABLE tasks ADD COLUMN status TEXT NOT NULL DEFAULT 'TODO';
-- UPDATE tasks SET status = 'DONE' WHERE done = 1;
