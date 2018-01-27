CC=gcc
CFLAGS=-O0 -g -S -std=c99 -ansi -pedantic
ASM=support.s


all: support.s

support.s: support.c
	$(CC) $(CFLAGS) support.c -o $(ASM)

clean:
	rm -f support.s
