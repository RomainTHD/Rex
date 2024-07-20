package fr.rthd.checker;

import java.util.List;

public abstract class LittleEndianChecker extends Checker {
	protected LittleEndianChecker(List<Byte> bytes) {
		super(bytes);
	}

	@Override
	protected long nextU16() {
		return next() + next() * (1 << 8);
	}

	@Override
	protected long nextU32() {
		return next() + next() * (1 << 8) + next() * (1 << 16) + next() * (1 << 24);
	}

}
