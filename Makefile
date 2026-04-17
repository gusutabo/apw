CC = gcc
CFLAGS = -Wall -Wextra -Iinclude
SRC = src/main.c src/db.c src/cli.c
OUT = apw

all:
	$(CC) $(CFLAGS) $(SRC) -o $(OUT) -lsqlite3

run:
	./$(OUT)

clean:
	rm -f $(OUT)
