package fr.rthd.checker;

import fr.rthd.common.Logger;

import java.util.List;

public abstract class LittleEndianChecker extends Checker {
	private static final Logger logger = new Logger(Checker.class);

	protected LittleEndianChecker(List<Byte> bytes) {
		super(bytes);
	}

	@Override
	protected int nextU16() {
		var v = nextU8(false) + nextU8(false) * (1 << 8);
		logger.debug(String.format("0x%1$04X", v));
		return v;
	}

	@Override
	protected long nextU32() {
		var v = nextU8(false)
			+ (long) nextU8(false) * (1 << 8)
			+ (long) nextU8(false) * (1 << 16)
			+ (long) nextU8(false) * (1 << 24);
		logger.debug(String.format("0x%1$08X", v));
		return v;
	}
}
