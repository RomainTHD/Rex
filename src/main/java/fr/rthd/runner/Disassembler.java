package fr.rthd.runner;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.common.Utils;
import fr.rthd.io.LittleEndianDataManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Disassembler {
	private static final Logger logger = new Logger(Disassembler.class);
	private final LittleEndianDataManager virtualSpace;
	private final Registers registers;

	private boolean canContinue = true;

	public boolean step() {
		int opCode = nextU8();
		switch (opCode) {
			case 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 -> add();
			case 0x06, 0x07 -> illegalOpCode(opCode);
			case 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d -> or();
			case 0x0e -> illegalOpCode(opCode);
			case 0x20, 0x21, 0x22, 0x23, 0x24, 0x25 -> and();
			case 0x30, 0x31, 0x32, 0x33, 0x34, 0x35 -> xor();
			case 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57 -> push(opCode);
			case 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f -> pop(opCode);
			case 0x88, 0x89, 0x8a, 0x8b, 0x8c -> mov(opCode);
			case 0x90 -> nop();
			case 0xc2, 0xc3 -> ret();
			default -> throw FailureManager.fail(
				Disassembler.class,
				ExitCode.InvalidState,
				"Unsupported opcode " + Utils.u8ToString(opCode)
			);
		}
		return canContinue;
	}

	private int getRegister(int value) {
		if (value / 0b1000 != 0) {
			// TODO: support this
		}

		return registers.intToReg(value);
	}

	private int registerOperandLeft(int value) {
		return getRegister(value);
	}

	private int registerOperandRight(int value) {
		return getRegister(value >> 3);
	}

	private void illegalOpCode(int opCode) {
		throw FailureManager.fail(
			Disassembler.class,
			ExitCode.IllegalOperation,
			"Illegal opcode " + Utils.u8ToString(opCode)
		);
	}

	private int nextU8() {
		var i = virtualSpace.readU8();
		// logger.debug("Reading " + Utils.u8ToString(i));
		return i;
	}

	/******************************************************************************************************************/

	private void add() {
		logger.debug("ADD");
	}

	private void or() {
		logger.debug("OR");
	}

	private void and() {
		logger.debug("AND");
	}

	private void xor() {
		var b = nextU8();
		var r1 = registerOperandLeft(b);
		var r2 = registerOperandRight(b);
		logger.debug(String.format("XOR %s, %s", registers.registerName(r1), registers.registerName(r2)));
		registers.set(r1, registers.get(r1) ^ registers.get(r2));
	}

	private void push(int opCode) {
		var reg = registerOperandLeft(opCode);
		logger.debug("PUSH " + registers.registerName(reg));
	}

	private void pop(int opCode) {
		var reg = getRegister(opCode);
		logger.debug("POP " + registers.registerName(reg));
	}

	private void mov(int opCode) {
		var b = nextU8();
		var r1 = registerOperandLeft(b);
		var r2 = registerOperandRight(b);
		logger.debug(String.format("MOV %s, %s", registers.registerName(r1), registers.registerName(r2)));
		registers.set(r1, registers.get(r2));
	}

	private void nop() {
		logger.debug("NOP");
	}

	private void ret() {
		logger.debug("RET");
		// TODO: change this behaviour
		canContinue = false;
	}
}
