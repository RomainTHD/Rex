package fr.rthd.runner;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.common.Utils;

public class Registers {
	private static final Logger logger = new Logger(Registers.class);

	public final int EAX = 0b000;
	public final int ECX = 0b001;
	public final int EDX = 0b010;
	public final int EBX = 0b011;
	public final int ESP = 0b100;
	public final int EBP = 0b101;
	public final int ESI = 0b110;
	public final int EDI = 0b111;
	private final long[] values;

	public Registers() {
		values = new long[8];
	}

	public String registerName(int value) {
		return switch (value) {
			case 0b000 -> "EAX";
			case 0b001 -> "ECX";
			case 0b010 -> "EDX";
			case 0b011 -> "EBX";
			case 0b100 -> "ESP";
			case 0b101 -> "EBP";
			case 0b110 -> "ESI";
			case 0b111 -> "EDI";
			default -> throw FailureManager.fail(Registers.class, ExitCode.InvalidState);
		};
	}

	public int intToReg(int value) {
		return value % 0b1000;
	}

	public long get(int reg) {
		return values[intToReg(reg)];
	}

	public void set(int reg, long value) {
		values[intToReg(reg)] = value;
	}

	public void dump() {
		for (int i = 0b000; i <= 0b111; ++i) {
			logger.debug(String.format("%s: %s", registerName(i), Utils.u32ToString(get(i))));
		}
	}
}
