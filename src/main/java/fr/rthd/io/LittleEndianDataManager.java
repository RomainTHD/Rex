package fr.rthd.io;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Utils;
import lombok.Builder;

public class LittleEndianDataManager {
	private final int[] bytes;
	private int idx = 0;

	@Builder
	public LittleEndianDataManager(int[] bytes) {
		this.bytes = bytes;
	}

	public int readU8() {
		if (idx >= bytes.length) {
			throw FailureManager.fail(
				LittleEndianDataManager.class,
				ExitCode.InvalidFile,
				"Tried to read after end of file"
			);
		}
		return bytes[idx++];
	}

	public int readU16() {
		return readU8() + readU8() * (1 << 8);
	}

	public long readU32() {
		return readU8()
			+ (long) readU8() * (1 << 8)
			+ (long) readU8() * (1 << 16)
			+ (long) readU8() * (1 << 24);
	}

	public void writeU8(int value) {
		if (value > 0xff || value < 0) {
			throw FailureManager.fail(
				LittleEndianDataManager.class,
				ExitCode.InvalidState,
				String.format(
					"Tried to insert value %s @%s",
					Utils.u32ToString(value),
					Utils.u32ToString(idx)
				)
			);
		}

		this.bytes[idx++] = value;
	}

	public void jumpAt(int dst) {
		if (dst > size()) {
			throw FailureManager.fail(LittleEndianDataManager.class, ExitCode.InvalidFile, "Destination out of bounds");
		}
		idx = dst;
	}

	public int readAt(int dst) {
		if (dst > size()) {
			throw FailureManager.fail(LittleEndianDataManager.class, ExitCode.InvalidFile, "Destination out of bounds");
		}
		return bytes[dst];
	}

	public int size() {
		return bytes.length;
	}
}
