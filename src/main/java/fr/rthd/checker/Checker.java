package fr.rthd.checker;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;

import java.util.List;

public abstract class Checker {
	private static final Logger logger = new Logger(Checker.class);
	private int _idx = 0;
	protected List<Byte> _bytes;

	protected Checker(List<Byte> bytes) {
		this._bytes = bytes;
	}

	public abstract void check();

	protected long next() {
		logger.debug(Integer.toHexString(Byte.toUnsignedInt(_bytes.get(_idx))));
		return Byte.toUnsignedLong(_bytes.get(_idx++));
	}

	protected abstract long nextU16();

	protected abstract long nextU32();

	protected void skipAt(long dst) {
		if (dst > (long) Integer.MAX_VALUE) {
			throw FailureManager.fail(ExitCode.InvalidFile, "Destination out of bounds");
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
