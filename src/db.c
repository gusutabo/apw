#include <stdio.h>
#include <stdlib.h>
#include <sqlite3.h>

#define DB_FILE "tasks.db"

sqlite3* db_open()
{
    sqlite3 *db;
    if (sqlite3_open(DB_FILE, &db) != SQLITE_OK)
        return NULL;

    return db;
}

void db_init()
{
    sqlite3 *db = db_open();
    if (!db) return;

    const char *sql =
        "CREATE TABLE IF NOT EXISTS tasks ("
        "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        "name TEXT NOT NULL,"
        "description TEXT,"
        "done INTEGER DEFAULT 0);";
    
    sqlite3_exec(db, sql, 0, 0, 0);
    sqlite3_close(db);
}

int db_save_task(const char *name, const char *desc)
{
    sqlite3 *db = db_open();
    if (!db) return -1;

    const char *sql = "INSERT INTO tasks (name, description) VALUES (?, ?);";
    sqlite3_stmt *stmt;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, 0) != SQLITE_OK)
    {
        sqlite3_close(db);
        return -1;
    }

    sqlite3_bind_text(stmt, 1, name, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(stmt, 2, desc, -1, SQLITE_TRANSIENT);

    int rc = sqlite3_step(stmt);

    sqlite3_finalize(stmt);
    sqlite3_close(db);

    return (rc == SQLITE_DONE) ? 0 : -1;
}

int db_list_tasks()
{
    sqlite3 *db = db_open();
    if (!db) return -1;

    const char *sql = "SELECT id, name, description, done FROM tasks;";
    sqlite3_stmt *stmt;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, 0) != SQLITE_OK)
    {
        sqlite3_close(db);
        return -1;
    }

    while (sqlite3_step(stmt) == SQLITE_ROW)
    {
        int id = sqlite3_column_int(stmt, 0);
        const unsigned char *name = sqlite3_column_text(stmt, 1);
        const unsigned char *desc = sqlite3_column_text(stmt, 2);
        int done = sqlite3_column_int(stmt, 3);

        printf("[%d] %s %s - %s\n",
               id,
               done ? "✔" : " ",
               name ? (const char*)name : "",
               desc ? (const char*)desc : "");
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);

    return 0;
}

int db_delete_task(int id)
{
    sqlite3 *db = db_open();
    if (!db) return -1;

    const char *sql = "DELETE FROM tasks WHERE id = ?;";
    sqlite3_stmt *stmt;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, 0) != SQLITE_OK)
    {
        sqlite3_close(db);
        return -1;
    }

    sqlite3_bind_int(stmt, 1, id);

    int rc = sqlite3_step(stmt);
    int changes = sqlite3_changes(db);

    sqlite3_finalize(stmt);
    sqlite3_close(db);

    return (rc == SQLITE_DONE && changes > 0) ? 0 : -1;
}

int db_complete_task(int id)
{
    sqlite3 *db = db_open();
    if (!db) return -1;

    const char *sql = "UPDATE tasks SET done = 1 WHERE id = ?;";
    sqlite3_stmt *stmt;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, 0) != SQLITE_OK)
    {
        sqlite3_close(db);
        return -1;
    }

    sqlite3_bind_int(stmt, 1, id);

    int rc = sqlite3_step(stmt);
    int changes = sqlite3_changes(db);

    sqlite3_finalize(stmt);
    sqlite3_close(db);

    return (rc == SQLITE_DONE && changes > 0) ? 0 : -1;
}
