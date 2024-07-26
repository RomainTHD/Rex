	.file	"file.c"
	.section	.text$__main,"x"
	.globl	___main
	.def	___main;	.scl	2;	.type	32;	.endef
___main:
	pushl	%ebp
	xorl	%eax, %eax
	movl	%esp, %ebp
	popl	%ebp
	ret
