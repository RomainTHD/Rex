package fr.rthd.checker;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;

import java.util.List;

public abstract class Checker {
	private static final Logger logger = new Logger(Checker.class);
	protected List<Byte> _bytes;
	private int _idx = 0;

	protected Checker(List<Byte> bytes) {
		this._bytes = bytes;
	}

	public abstract void check();

	protected int nextU8(boolean log) {
		if (log) {
			logger.debug(String.format("0x%1$02X", Byte.toUnsignedInt(_bytes.get(_idx))));
		}
		return Byte.toUnsignedInt(_bytes.get(_idx++));
	}

	protected int nextU8() {
		return nextU8(true);
	}

	protected abstract int nextU16();

	protected abstract long nextU32();

	protected void skipAt(long dst) {
		if (dst > (long) Integer.MAX_VALUE) {
			throw FailureManager.fail(Checker.class, ExitCode.InvalidFile, "Destination out of bounds");
		}
		_idx = (int) dst;
	}

	protected long previous() {
		return _bytes.get(--_idx);
	}

	protected long at() {
		return _bytes.get(_idx);
	}
}
