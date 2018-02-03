.globl _main
_main:
subq	$24, %rsp
movq	$5, -16(%rsp)
addq	$10, -16(%rsp)
movq	-16(%rsp), %rax
movq	%rax, -24(%rsp)
movq	-24(%rsp), %rax
movq	%rax, -8(%rsp)
addq	$13, -8(%rsp)
addq	$24, %rsp
retq