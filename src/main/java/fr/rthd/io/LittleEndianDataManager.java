package fr.rthd.io;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.common.Utils;
import lombok.Builder;

public class LittleEndianDataManager {
	private static final Logger logger = new Logger(LittleEndianDataManager.class);
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

	public int getIdx() {
		return idx;
	}

	public void dump() {
		StringBuilder sb;

		logger.debug(" ".repeat(5) + "+" + "-".repeat(49) + "+");

		sb = new StringBuilder();
		sb.append("     | ");
		for (int i = 0; i < 16; i++) {
			sb.append(" ")
			  .append(Integer.toHexString(i))
			  .append(" ");
		}
		sb.append("|");
		logger.debug(sb.toString());

		logger.debug("-".repeat(5) + "+" + "-".repeat(49) + "+");

		var ellipsis = false;

		for (int row = 0; row <= size() / 16; ++row) {
			sb = new StringBuilder();
			var sum = 0;

			sb.append(String.format("%1$3XX", row));
			sb.append(" | ");

			for (int col = 0; col < 16; col++) {
				var idx = row * 16 + col;
				var cell = 0;
				if (idx < size()) {
					cell = readAt(idx);
				}
				sum += cell;
				sb.append(Utils.u8ToString(cell, false));
				sb.append(" ");
			}
			sb.append("|");

			if (sum == 0) {
				if (!ellipsis) {
					logger.debug(" ... |" + " ".repeat(49) + "|");
					ellipsis = true;
				}
			} else {
				logger.debug(sb.toString());
				ellipsis = false;
			}
		}

		logger.debug("-".repeat(5) + "+" + "-".repeat(49) + "+");
	}
}
