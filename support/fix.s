.globl _main
_main:
pushq	%rbp
movq	%rsp, %rbp
movq	$5, %r12
cmpq	$17, %r12
jg		tif_1
jmp		fif_1
tif_1:
movq	$13, %r12
jmp		if_1_end
fif_1:
movq	$25, %r12
jmp		if_1_end
if_1_end:
movq	%r12, %rax
movq	%rbp, %rsp
popq	%rbp
retq
