#ifndef CLI_H
#define CLI_H

#define RED "\033[31m"
#define GREEN "\033[32m"
#define BOLD "\033[1m"
#define RESET "\033[0m"

int verify_args(int argc, char *args[]);
void error(const char *message);

#endif