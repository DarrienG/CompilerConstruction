#ifndef __SUPPORT_H
#define __SUPPORT_H

#include "uthash.h"

#define TAG_INT 0
#define TAG_BOOL 1
#define TAG_VEC 2

typedef struct heap_obj {
    int64_t id;
    int64_t* val;
    UT_hash_handle hh;
} ho, *hop;

extern int64_t* root_stack_ptr;
extern int64_t* heap_ptr;
extern int64_t* free_ptr;
extern int64_t* from_space_ptr;
extern int64_t* to_space_ptr;

int64_t amt_alloc;
int64_t g_heap_size;
int64_t root_stack_size;
int64_t used_space;
int64_t root_size;
hop h_table;

bool init(int64_t, int64_t);
int64_t*gc_copy(int64_t**, int64_t*);
void* gc_malloc(int size);
int64_t collect(int64_t*);
void gc_destroy();

void print(int);
int64_t read(void);

int memround(int64_t size);
int memtrunc(int64_t size);
void end(int signo);

bool root_stack_push(int64_t* object);
int64_t* root_stack_pop(void);

void add_ho(hop h);
hop find_ho(int64_t id);

#endif  // __SUPPORT_H
