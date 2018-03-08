CC=gcc-7
CFLAGS=-O0 -g -S -std=c99 -ansi -pedantic 
ASM=support.s

all: support.s
	$(CC) test.s support.s -o pls

clone:
	cp ../main/test.s .
	make all

run: all support.s
	./pls

rcl: clone
	./pls
	echo $?

copy: support.s
	pbpaste > test.s
	$(CC) test.s support.s -o pls

support.s: support.c
	$(CC) $(CFLAGS) support.c -o $(ASM)

clean:
	rm -f test.s support.s pls ../main/test.s a.out
