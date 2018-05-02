#ifndef SUPPORT_H
#define SUPPORT_H

#include <sys/mman.h>
#include <signal.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <sys/types.h>

#include "unistd.h"

#define TAG_INT  0
#define TAG_BOOL 1
#define TAG_VEC  2
#define TAG_ARR  3

#define roundp(x) (int64_t)((getpagesize() - 1 + x) & (~(getpagesize() - 1)))
#define roundpd(x) (int64_t)(x & (~(getpagesize() - 1)))

extern int64_t *root_stack_ptr, *free_ptr, *from_space_ptr, *heap_ptr, *to_space_ptr;
extern int64_t heap_size, root_size;

bool gc_init(int64_t, int64_t);
void* gc_malloc(int);
int64_t* gc_copy(int64_t**, int64_t*);
void gc_collect(int64_t*);
void gc_destroy(void);

bool root_stack_push(int64_t*);
int64_t* root_stack_pop(void);

void sig_handler(int);


#endif  // SUPPORT_H
