#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>

#include "../include/cli.h"
#include "../include/db.h"

void usage();
void ok(const char *message) { printf(GREEN "[OK]" RESET " %s\n", message); }
void error(const char *message) { printf(RED "[ERROR]" RESET " %s\n", message); }

int check_args(int argc, int expected, char *message)
{
    if (argc < expected) {
        error(message);
        usage();
        return -1;
    }

    return 0;
}

static int parse_id(const char *str, long *out)
{
    char *end;
    errno = 0;
    long id = strtol(str, &end, 10);

    if (*end != '\0' || errno != 0 || id <= 0) {
        error("Invalid ID: must be a positive integer.");
        return -1;
    }

    *out = id;
    return 0;
}

void cmd_add(int argc, char *argv[])
{
    if (check_args(argc, 4, "Usage: add <name> <description>.") != 0) return;

    db_save_task(argv[2], argv[3]);
    ok("Task added!");
}

void cmd_delete(int argc, char *argv[])
{
    if (check_args(argc, 3, "Usage: delete <id>") != 0) return;

    long id;
    if (parse_id(argv[2], &id) != 0) return;

    if (db_delete_task((int)id) == 0)
        ok("Task deleted.");
    else
        error("Failed to delete task.");
}

void cmd_complete(int argc, char *argv[])
{
    if (check_args(argc, 3, "Usage: complete <id>") != 0) return;

    long id;
    if (parse_id(argv[2], &id) != 0) return;

    if (db_complete_task((int)id) == 0)
        ok("Marked as complete.");
    else
        error("Failed to update task.");
}

void cmd_list(int argc, char *argv[])
{
    (void)argc;
    (void)argv;
    db_list_tasks();
}

void cmd_help(int argc, char *argv[])
{
    (void)argc;
    (void)argv;
    usage();
}

typedef struct
{
    const char *name;
    const char *alias;
    const char *usage;
    const char *description;
    void (*handler)(int argc, char *argv[]);
} Command;

const Command commands[] = {
    {"add",      "-a", "add <task> <description>", "Creates a new task",        cmd_add},
    {"list",     "-l", "list",                     "Lists all tasks",            cmd_list},
    {"complete", "-c", "complete <id>",            "Marks a task as completed",  cmd_complete},
    {"delete",   "-d", "delete <id>",              "Deletes a task",             cmd_delete},
    {"help",     "-h", "help",                     "Shows this screen",          cmd_help},
};

void usage()
{
    int n = sizeof(commands) / sizeof(commands[0]);

    printf(BOLD "Usage:\n" RESET "  ./apw <command> <arguments>\n\n");
    printf(BOLD "COMMANDS:\n" RESET);

    for (int i = 0; i < n; i++)
        printf("  %-5s %-10s %-20s  %s\n",
               commands[i].alias,
               commands[i].name,
               commands[i].usage,
               commands[i].description);
}

int verify_args(int argc, char *args[])
{
    if (check_args(argc, 2, "You need to pass one argument.") != 0) return -1;

    int n = sizeof(commands) / sizeof(commands[0]);

    for (int i = 0; i < n; i++) {
        if (strcmp(args[1], commands[i].name) == 0 ||
            strcmp(args[1], commands[i].alias) == 0)
        {
            commands[i].handler(argc, args);
            return 0;
        }
    }

    error("Invalid command.\n");
    usage();
    return -1;
}
