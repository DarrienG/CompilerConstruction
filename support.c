#include <signal.h>
#include <stdio.h>
#include <stdbool.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <math.h>

#include "support.h"
#include "uthash.h"
#include "unistd.h"

int64_t* root_stack_ptr = NULL;
int64_t* heap_ptr = NULL;
int64_t* free_ptr = NULL;
int64_t* from_space_ptr = NULL;
int64_t* to_space_ptr = NULL;
int64_t root_stack_capacity;

void print(int arg) {
    printf("%d\n", arg);
}

int64_t read(void) {
    int arg;
    scanf("%d", &arg);
    return arg;
}

void add_ho(hop s) {
    HASH_ADD(hh, h_table, id, sizeof(int64_t), s);
}

hop find_ho(int64_t id) {
    hop out;
    HASH_FIND(hh, h_table, &id, sizeof(int64_t*), out);  
    return out;
}

void* gc_malloc(int size) {
    if (size < 0) return NULL;
    int64_t* tmp;
    if (used_space + size > g_heap_size) collect(NULL);
    if (used_space + size > g_heap_size) return NULL;

    used_space += size;
    tmp = free_ptr;
    free_ptr += (int64_t) ceil((double)size / (double)sizeof(int64_t));

    return (void*)tmp;
}

bool init(int64_t heap_size, int64_t root_stack_size) {
    struct sigaction handler;
    handler.sa_handler = end;
    g_heap_size = heap_size;
    used_space = 0;

    amt_alloc = memround(heap_size*2);

    sigemptyset(&handler)
    sigaction(SIGINT, &handler, 0);
    sigaction(SIGSEGV, &handler, 0);
    sigaction(SIGTERM, &handler, 0);

    to_space_ptr = free_ptr = heap_ptr = (int64_t*) mmap(NULL, amt_alloc, PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);

    if (heap_ptr == NULL) {
        end(-1);
    }

    used_space = 0;

    from_space_ptr = (int64_t*) ((int64_t)heap_ptr + (heap_size / 2));
    if (mprotect(from_space_ptr, heap_size / 2, PROT_NONE) < 0) {
        /* We failed, terminate */
        end(-1);
    }

    root_stack_ptr = (int64_t*)malloc(sizeof(void*) * root_stack_size);
    if (root_stack_ptr == NULL) {
        printf("Unable to allocate heap stack");
        exit(-4);
    }

    root_stack_capacity = root_stack_size;

    root_size = 0;
    return true;
}

int64_t* gc_copy(int64_t** from_free_ptr, int64_t* obj) {
    hop temp = NULL;
    int64_t* from_res_ptr;
    int64_t i;

    if (memround((int64_t)*from_free_ptr) == memround((int64_t)obj)) {
        return obj;
    }

    if (obj[0] == TAG_INT || obj[0] == TAG_BOOL) {
        find_ho((int64_t)obj);
        if (temp == NULL) {
            /* Add record to htable */
            temp = (hop) malloc(sizeof(ho));
            temp -> id = (int64_t) obj;
            temp -> val = (*from_free_ptr);

            add_ho(temp);

            /* Copy my dudes */
            (*from_free_ptr)[0] = obj[0]; /* Copies tag */
            (*from_free_ptr)[1] = obj[1];
            *from_free_ptr += 2;
        }
        return temp -> val;
    }

    temp = find_ho((int64_t)obj);
    if (temp == NULL) {
        /* Add record to htable */
        temp = (hop) malloc(sizeof(ho));
        temp -> val = from_res_ptr = *from_free_ptr;
        temp -> id = (int64_t)obj;
        add_ho(temp);

        /* Perform the copy */
        *from_free_ptr += 2 + obj[1];
        from_res_ptr[0] = obj[0];
        from_res_ptr[1] = obj[1];

        for (i = 2; i < 2 + obj[1]; ++i)  {
            from_res_ptr[i] = (int64_t)gc_copy(from_free_ptr, (int64_t*)obj[i]);
        }
    }

    return (int64_t*)(temp -> id);
}

int64_t collect(int64_t* root_stack_top) {
    int64_t* from_free_ptr = from_space_ptr;
    if (mprotect(from_space_ptr, g_heap_size / 2, PROT_READ | PROT_WRITE < 0)) {
        /* We failed, terminate */
        end(-1);
    }

    for (; root_stack_top != root_stack_ptr; --root_stack_top) { 
        gc_copy(&from_free_ptr, root_stack_top);
    }

    ho *current_obj, *tmp;

    HASH_ITER(hh, h_table, current_obj, tmp) {
        HASH_DEL(h_table, current_obj);  /* delete; users advances to next */
        free(current_obj);            /* optional- if you want to free  */
    }

    int64_t* swp = to_space_ptr;
    to_space_ptr = from_space_ptr;
    from_space_ptr = swp;

    if (mprotect(from_space_ptr, g_heap_size / 2, PROT_NONE < 0)) {
        /* We failed, terminate */
        end(-1);
    }
}

int memround(int64_t size) {
    int less_page_size = getpagesize() - 1;
    return ((int64_t)(size)) & (~less_page_size);
}

int memtrunc(int64_t size) {
    int less_page_size = getpagesize() - 1;
    return (less_page_size + (int64_t)(size)) & (~less_page_size);
}


bool root_stack_push(int64_t* object) {
    if (root_size + 1 >= root_stack_capacity)
        return false;
    root_size += 1;
    *(root_stack_ptr + root_size) = (int64_t)object;
    return true;
}

int64_t* root_stack_pop(void) {
    int64_t* tmp = NULL;
    if (root_size - 1 >= 0 ) {
        tmp = (int64_t*)root_stack_ptr[root_size];
        root_size -= 1;
    }
    return tmp;
}

void gc_destroy() {
    end(0);
}

void end(int signo) {
    munmap(free_ptr, amt_alloc);
}
