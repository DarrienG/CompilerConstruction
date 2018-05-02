CC=gcc-7
CFLAGS=-O0 -g -S -std=gnu99 -pedantic 
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

gc: 
	$(CC) gc_test.c support.c -lm -o gc_test
	./gc_test

copy: support.s
	pbpaste > test.s
	$(CC) test.s support.s -lm -o pls

support.s: support.c
	$(CC) $(CFLAGS) support.c -o $(ASM)

clean:
	rm -f gc_test test.s support.s pls ../main/test.s a.out
