#ifndef DB_H
#define DB_H

int db_init();
int db_save_task(const char *name, const char *desc);
int db_list_tasks();
int db_delete_task(int id);
int db_complete_task(int id);

int db_start_pomodoro(const char *start_time);
int db_stop_pomodoro(const char *end_time, int duration);

#endif
