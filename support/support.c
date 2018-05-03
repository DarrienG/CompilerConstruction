#include "support.h"
#include "uthash.h"

static const int signals[] = {SIGHUP, SIGINT, SIGQUIT, SIGBUS, SIGTERM, SIGSEGV, SIGFPE};
static const unsigned n_signals = sizeof(signals) / sizeof(int);

typedef struct {
    int64_t id;
    int64_t* val;
    UT_hash_handle hh;
} heap_object;

int64_t* root_stack_ptr = NULL;
int64_t* free_ptr       = NULL;
int64_t* to_space_ptr   = NULL;
int64_t* from_space_ptr = NULL;
int64_t* heap_ptr       = NULL;
int64_t  heap_size, root_size, root_stack_capacity, used_space;
heap_object* hObjects = NULL;

void add_heap_object(heap_object* heap_object_ptr) {
    HASH_ADD(hh, hObjects, id, sizeof(int64_t), heap_object_ptr);
}

heap_object* find_heap_object(int64_t object_id) {
    heap_object* s;

    HASH_FIND(hh, hObjects, &object_id, sizeof(int64_t*), s);

    return s;
}

void sig_handler(int sig) {
    munmap(heap_ptr, heap_size);
    exit(sig);
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

bool gc_init(int64_t new_heap_size, int64_t root_stack_size) {
    sigset_t sig_mask;
    struct sigaction sig_action;
    unsigned i;

    sigemptyset(&sig_mask);
    for (i = 0; i < n_signals; ++i)
        sigaddset(&sig_mask, signals[i]);
    for (i = 0; i < n_signals; ++i) {
        sig_action.sa_handler = sig_handler;
        sig_action.sa_mask = sig_mask;
        sig_action.sa_flags = 0;
        if (sigaction(signals[i], &sig_action, NULL) < 0) {
            perror("Unable to set up GC signal handler");
            exit(-1);
        }
    }

    heap_size = roundp(new_heap_size) * 2;

    heap_ptr = (int64_t*)mmap(NULL, heap_size, PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);
    if (heap_ptr == NULL) {
        perror("failed to allocate space for GC");
        sig_handler(-2);
    }

    free_ptr = to_space_ptr = heap_ptr;
    used_space = 0;
    from_space_ptr = (int64_t*)((int64_t)heap_ptr + (heap_size / 2));
    if (mprotect(from_space_ptr, heap_size / 2, PROT_NONE) < 0) {
        perror("Unable to protect GC from space");
        sig_handler(-3);
    }

    root_stack_ptr = (int64_t*)malloc(sizeof(void*) * root_stack_size);
    if (root_stack_ptr == NULL) {
        printf("Unable to allocate heap stack");
        exit(-4);
    }

    root_stack_capacity = root_stack_size;
    root_size = 0;
}

void* gc_malloc(int size) {
    int64_t* temp;
    if (size < 0)
        return NULL;
    if (used_space + size > heap_size / 2)
        gc_collect(root_stack_ptr + root_size);
    if (used_space + size > heap_size / 2)
        return NULL;

    used_space += size;
    temp = free_ptr;
    free_ptr += (int64_t)ceil((float)size / sizeof(int64_t));

    return (void*)temp;
}

int64_t* gc_copy(int64_t** from_free_ptr, int64_t* object) {
    heap_object* temp = NULL;
    int64_t* from_res_ptr;
    int64_t i;

    if (roundpd(((int64_t)*from_free_ptr)) == roundpd((int64_t)object)){
        return object; 
    }

    if(object[0] == TAG_INT || object[0] == TAG_BOOL) {
        temp = find_heap_object((int64_t)object);
        if (temp == NULL) {
            /* Adds record to hash table */
            temp = (heap_object*)malloc(sizeof(heap_object));
            temp->val = (*from_free_ptr);
            temp->id = (int64_t)object;
            add_heap_object(temp);

            /* Preform the copy */
            (*from_free_ptr)[0] = object[0]; /* copy the tag */
            (*from_free_ptr)[1] = object[1]; /* copy the data */
            *from_free_ptr += 2;
        }
        return temp->val;
    }

    temp = find_heap_object((int64_t)object);
    if (temp == NULL) {
        /* Adds record to hash table */
        temp = (heap_object*)malloc(sizeof(heap_object));
        temp->val = from_res_ptr = (*from_free_ptr);
        temp->id = (int64_t)object;
        add_heap_object(temp);

        /* perform the copy */
        *from_free_ptr += 2 + object[1];
        from_res_ptr[0] = object[0];
        from_res_ptr[1] = object[1];

        for (i = 2; i < 2 + object[1]; ++i) {
            from_res_ptr[i] = (int64_t)gc_copy(from_free_ptr, (int64_t*)object[i]);
        }
    }
    return temp->val;
}

void gc_collect(int64_t* root_stack_top) {
    int64_t* from_free_ptr = from_space_ptr;
    int64_t* swp, *runner;

    if (mprotect(from_space_ptr, heap_size / 2, PROT_READ | PROT_WRITE) < 0) {
        perror("Unable to unprotect GC from space");
        sig_handler(-4);
    }


    for (runner = root_stack_top; runner != root_stack_ptr; --runner) {
        *runner = (int64_t)gc_copy(&from_free_ptr, (int64_t*)*runner);
    }

    heap_object *current_object, *tmp;

    HASH_ITER(hh, hObjects, current_object, tmp) {
        HASH_DEL(hObjects, current_object);  /* delete; users advances to next */
        free(current_object);            /* optional- if you want to free  */
    }

    hObjects = NULL;

    /* Swap the pointers */
    swp = from_space_ptr;
    from_space_ptr = to_space_ptr;
    to_space_ptr = swp;

    if (mprotect(from_space_ptr, heap_size / 2, PROT_NONE) < 0) {
        perror("Unable to protect GC from space after collect");
        sig_handler(-5);
    }
}

void gc_destroy(void) {
    sig_handler(0);
}

void print(int arg) {
    printf("%d\n", arg);
}

int read() {
    int arg;
    scanf("%d", &arg);
    return arg;
}
