#include <stdio.h>

void print(int);
int read(void);


void print(int arg) {
        printf("%d\n", arg);
}

int read(void) {
        int arg;
        scanf("%d", &arg);
        return arg;
}
