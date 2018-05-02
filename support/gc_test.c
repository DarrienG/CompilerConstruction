#include <stdio.h>
#include "support.h"

#define EQ 1
#define NE 2
#define LT 3
#define GT 4

static int caseCounter = 0;
static int casePassed = 0;

void assert(int64_t lhs, int equality, int64_t rhs) {
    caseCounter++;
    switch (equality) {
        case EQ:
            if (lhs != rhs) {
                printf("FAILED CASE %d: %x != %x\n", caseCounter, lhs, rhs);
                return;
            }
            break;
        case NE:
            if (lhs == rhs) {
                printf("FAILED CASE %d: %d == %d\n", caseCounter, lhs, rhs);
                return;
            }
            break;
        case LT:
            if (lhs >= rhs) {
                printf("FAILED CASE %d: %d >= %d\n", caseCounter, lhs, rhs);
                return;
            }
            break;
        case GT:
            if (lhs <= rhs) {
                printf("FAILED CASE %d: %d <= %d\n", caseCounter, lhs, rhs);
                return;
            }
            break;
        default:
            printf("FAILED CASE %d: Unrecognised test condition\n", caseCounter);
    }
    casePassed++;
    return;
}

int main(void) {
    bool test = gc_init(4090, 4090);

    if (test == true)
        printf("Created GC env!\nHeap Total: %d\nHeap effective: %d\n",
                heap_size, heap_size/2);
    else
        printf("failed to create GC env!\n");


    int64_t *arr1, *arr2, *arr3, *arrNc;

    printf("getting space\n");

    arr1 = gc_malloc(sizeof(int64_t) * 2);
    arr2 = gc_malloc(sizeof(int64_t) * 3);
    arr3 = gc_malloc(sizeof(int64_t) * 4);
    arrNc = gc_malloc(sizeof(int64_t) * 2);

    printf("Setting values\n");

    arr1[0] = TAG_INT;
    arr1[1] = 5;

    arr2[0] = TAG_VEC;
    arr2[1] = 1;
    arr2[2] = (int64_t)gc_malloc(sizeof(int64_t) * 2);
    ((int64_t*)arr2[2])[0] = TAG_BOOL;
    ((int64_t*)arr2[2])[1] = 1;

    arr3[0] = TAG_VEC;
    arr3[1] = 2;
    arr3[2] = (int64_t)arr1;
    arr3[3] = (int64_t)arr2;

    printf("pushing root stack\n");

    if (!root_stack_push(arr1) ||
        !root_stack_push(arr2) ||
        !root_stack_push(arr3)) {
        printf("GC failed to push elements onto the root stack\n");
        sig_handler(-1);
    }

    printf("calling collect\n");

    gc_collect(root_stack_ptr + root_size);

    printf("verifying results\n");

    arr3 = root_stack_pop();
    arr2 = root_stack_pop();
    arr1 = root_stack_pop();

    assert(arr3[2], EQ, (int64_t)arr1);
    assert(arr3[3], EQ, (int64_t)arr2);
    assert(((int64_t*)arr2[2])[1], EQ, 1);
    assert(arr1[1], EQ, 5);
    assert(roundpd((int64_t)from_space_ptr), EQ, roundpd((int64_t)arrNc));

    printf("%d of %d tests passed!\n", casePassed, caseCounter);

    gc_destroy();
    return 0;
}
