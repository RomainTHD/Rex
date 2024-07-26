echo _ > file.exe
rm file.exe
echo _ > file.s
rm file.s
gcc -Os -s -fdata-sections -ffunction-sections -nostdlib -ffreestanding -fno-asynchronous-unwind-tables -fno-unwind-tables -fno-ident -m32 -S          file.c
gcc -Os -s -fdata-sections -ffunction-sections -nostdlib -ffreestanding -fno-asynchronous-unwind-tables -fno-unwind-tables -fno-ident -m32 -o file.exe file.c
