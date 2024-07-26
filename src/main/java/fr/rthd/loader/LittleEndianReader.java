package fr.rthd.loader;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import lombok.Builder;

import java.util.List;

public class LittleEndianReader {
	private final List<Byte> bytes;
	private int idx = 0;

	@Builder
	public LittleEndianReader(List<Byte> bytes) {
		this.bytes = bytes;
	}

	public int nextU8() {
		if (idx >= bytes.size()) {
			throw FailureManager.fail(
				LittleEndianReader.class,
				ExitCode.InvalidFile,
				"Tried to read after end of file"
			);
		}
		return Byte.toUnsignedInt(bytes.get(idx++));
	}

	public int nextU16() {
		return nextU8() + nextU8() * (1 << 8);
	}

	public long nextU32() {
		return nextU8()
			+ (long) nextU8() * (1 << 8)
			+ (long) nextU8() * (1 << 16)
			+ (long) nextU8() * (1 << 24);
	}

	public void skipAt(long dst) {
		if (dst > (long) Integer.MAX_VALUE) {
			throw FailureManager.fail(Loader.class, ExitCode.InvalidFile, "Destination out of bounds");
		}
		idx = (int) dst;
	}

	public long previous() {
		return bytes.get(--idx);
	}

	public long at() {
		return bytes.get(idx);
	}
}
