#include <stdio.h>
#include "../include/cli.h"
#include "../include/db.h"

int main(int argc, char *argv[])
{
    db_init();
    
    if (verify_args(argc, argv) != 0)
        error("Something failed.");
    
    return 0;
}