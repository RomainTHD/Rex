package fr.rthd.runner;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.common.Pair;
import fr.rthd.common.Utils;
import fr.rthd.io.LittleEndianDataManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Disassembler {
	private static final Logger logger = new Logger(Disassembler.class);
	private final LittleEndianDataManager virtualMemory;
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
			case 0x83 -> immediateGroup();
			case 0x88, 0x89, 0x8c -> mov(false);
			case 0x8a, 0x8b -> mov(true);
			case 0x90 -> nop();
			case 0xb8, 0xb9, 0xba, 0xbb, 0xbc, 0xbd, 0xbe, 0xbf -> movLit(opCode);
			case 0xc2, 0xc3 -> ret();
			case 0xc9 -> leave();
			default -> throw FailureManager.fail(
				Disassembler.class,
				ExitCode.InvalidState,
				"Unsupported opcode " + Utils.u8ToString(opCode)
			);
		}
		return canContinue;
	}

	private int getRegister(int value) {
		if (value >= 0xc0) {
			return registers.intToReg(value);
		}

		throw new IllegalArgumentException("Register value out of range: " + Utils.u8ToString(value));
	}

	private Pair<RegisterOffset, RegisterOffset> getDoubleRegister(int value) {
		if (value >= 0xc0) {
			return new Pair<>(
				new RegisterOffset(registers.intToReg(value), 0),
				new RegisterOffset(registers.intToReg(value >> 3), 0)
			);
		}

		if (value >= 0x80) {
			throw FailureManager.fail(Disassembler.class, ExitCode.Unsupported, "32 bits offset not supported yet");
		}

		if (value >= 0x40) {
			var offset = nextU8();
			return new Pair<>(
				new RegisterOffset(registers.intToReg(value), offset >= 128 ? offset - 256 : offset),
				new RegisterOffset(registers.intToReg(value >> 3), 0)
			);
		}

		throw new IllegalArgumentException("Register value out of range: " + Utils.u8ToString(value));
	}

	private void illegalOpCode(int opCode) {
		throw FailureManager.fail(
			Disassembler.class,
			ExitCode.IllegalOperation,
			"Illegal opcode " + Utils.u8ToString(opCode)
		);
	}

	private int nextU8() {
		var i = virtualMemory.readU8();
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
		var reg = getDoubleRegister(b);
		logger.debug(String.format(
			"XOR %s, %s",
			registers.getRegName(reg.getLeft().getRegister()),
			registers.getRegName(reg.getRight().getRegister())
		));
		registers.set(
			reg.getLeft().getRegister(),
			registers.get(reg.getLeft().getRegister()) ^ registers.get(reg.getRight().getRegister())
		);
	}

	private void push(int opCode) {
		logger.debug("PUSH " + registers.getRegName(opCode));
		registers.set(Registers.ESP, registers.get(Registers.ESP) - 4);
		virtualMemory.writeU32((int) registers.get(Registers.ESP), registers.get(opCode));
	}

	private void pop(int opCode) {
		var reg = getRegister(opCode);
		logger.debug("POP " + registers.getRegName(reg));
		registers.set(
			reg,
			virtualMemory.readU32At((int) registers.get(Registers.ESP))
		);
		registers.set(Registers.ESP, registers.get(Registers.ESP) + 4);
	}

	private void sub(int nextOp) {
		var reg = getRegister(nextOp);
		var val = nextU8();
		logger.debug(String.format("SUB %s, %d", registers.getRegName(reg), val));
		registers.set(reg, registers.get(reg) - val);
	}

	private void immediateGroup() {
		var nextOp = nextU8();
		var mod = (nextOp >> 3) % 0b1000;
		switch (mod) {
			case 5 -> sub(nextOp);
			default -> throw FailureManager.fail(
				Disassembler.class,
				ExitCode.Unsupported,
				"Modifier not known: " + mod
			);
		}
	}

	private void mov(boolean flipped) {
		var b = nextU8();
		var reg =getDoubleRegister(b);
		if (flipped) {
			reg = new Pair<>(reg.getRight(), reg.getLeft());
		}
		var offset = reg.getRight().getOffset();
		logger.debug(String.format(
			"MOV %s, %s%s%s%s%s",
			registers.getRegName(reg.getLeft().getRegister()),
			offset == 0 ? "" : "[",
			registers.getRegName(reg.getRight().getRegister()),
			offset == 0 ? "" : offset > 0 ? " + " : " - ",
			offset == 0 ? "" : Math.abs(offset),
			offset == 0 ? "" : "]"
		));
		// TODO: should deref value
		registers.set(
			reg.getLeft().getRegister(),
			registers.get(reg.getRight().getRegister())
		);
	}

	private void nop() {
		logger.debug("NOP");
	}

	private void movLit(int opCode) {
		var r = getRegister(opCode);
		var lit = virtualMemory.readU32();
		logger.debug(String.format("MOV %s, %d", registers.getRegName(r), lit));
		registers.set(r, lit);
	}

	private void ret() {
		logger.debug("RET");
		// TODO: change this behaviour
		canContinue = false;
	}

	/**
	 * Synonym for
	 * <br />
	 * <code>
	 * mov  esp, ebp <br />
	 * pop  ebp
	 * </code>
	 */
	private void leave() {
		logger.debug("LEAVE");
	}
}
