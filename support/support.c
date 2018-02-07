#include <stdio.h>
#include <sys/types.h>

void print(int);
int64_t read(void);


void print(int arg) {
        printf("%d\n", arg);
}

int64_t read(void) {
        int arg;
        scanf("%d", &arg);
        return arg;
}
