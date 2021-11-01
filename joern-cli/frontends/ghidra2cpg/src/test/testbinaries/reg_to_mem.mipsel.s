.section .text

.global __start

#---------------------------------------- 
# compile and run on non mipsel platforms:
#
# $ mipsel-linux-gnu-as -O0 reg_to_mem.mipsel.s -o reg_to_mem.o && mipsel-linux-gnu-gcc-9 -O0 reg_to_mem.o -o reg_to_mem -static -nostdlib && qemu-mipsel ./reg_to_mem
# $ echo $?
# 42
#---------------------------------------- 
#
# (reg1 + reg2) ---> RAM --> reg3
#

__start:
	li $t0, 0x0
	li $t1, 0x28 # 40

	add $t2, $t1, $t0
	sw $t2, 42($sp)
	nop
	nop
	lw $t3, 42($sp)
	addiu $t4, $t3, 0x2

	or $t9, $t4, $zero
	nop

	li $v0, 0xfa1 # 4001
	or $a0, $t9, $zero
	syscall


.section .data
