CC=gcc
CFLAGS=-O0 -g -S -std=c99 -ansi -pedantic
ASM=support.s

all: 
	$(CC) test.s support.s -o pls

run: all support.s
	./pls

copy: support.s
	pbpaste > test.s
	$(CC) test.s support.s -o pls

support.s: support.c
	$(CC) $(CFLAGS) support.c -o $(ASM)

clean:
	rm -f test.s support.s pls
