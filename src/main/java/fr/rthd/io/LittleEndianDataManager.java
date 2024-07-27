package fr.rthd.io;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.common.Utils;

public class LittleEndianDataManager {
	private static final Logger logger = new Logger(LittleEndianDataManager.class);
	private final int[][] pages;
	private final int pageCount;
	private final int pageSize;
	private int idx = 0;

	public LittleEndianDataManager(int[] bytes) {
		this.pageSize = bytes.length;
		this.pageCount = 1;
		this.pages = new int[1][];
		this.pages[0] = bytes;
	}

	public LittleEndianDataManager(int pageSize, int pagesCount) {
		this.pageSize = pageSize;
		this.pageCount = pagesCount;
		this.pages = new int[pagesCount][];
	}

	public int readU8() {
		if (idx >= size()) {
			throw FailureManager.fail(
				LittleEndianDataManager.class,
				ExitCode.InvalidFile,
				"Tried to read outside of memory"
			);
		}

		var page = pages[idx / pageSize];
		var v = page == null ? 0 : page[idx % pageSize];
		idx++;
		return v;
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

		if (idx >= size()) {
			throw FailureManager.fail(
				LittleEndianDataManager.class,
				ExitCode.Segfault,
				"Tried to write outside of memory"
			);
		}

		var page = pages[idx / pageSize];
		if (page == null) {
			logger.debug("Created new page #" + idx / pageSize);
			page = new int[pageSize];
			pages[idx / pageSize] = page;
		}
		page[idx % pageSize] = value;
		idx++;
	}

	public void jumpAt(int dst) {
		if (dst > size()) {
			throw FailureManager.fail(LittleEndianDataManager.class, ExitCode.InvalidFile, "Destination out of bounds");
		}
		idx = dst;
	}

	public int readAt(int dst) {
		var prev = getPos();
		jumpAt(dst);
		var v = readU8();
		jumpAt(prev);
		return v;
	}

	public int size() {
		return pageCount * pageSize;
	}

	public int getPos() {
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

		for (int pageIdx = 0; pageIdx < pageCount; ++pageIdx) {
			if (pages[pageIdx] == null) {
				continue;
			}

			for (int row = 0; row <= pageSize / 16; ++row) {
				sb = new StringBuilder();
				var sum = 0;

				sb.append(String.format("%1$3XX", row + pageIdx * pageSize / 16));
				sb.append(" | ");

				for (int col = 0; col < 16; col++) {
					var idx = pageIdx * pageSize + row * 16 + col;
					var cell = readAt(idx);
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
		}

		logger.debug("-".repeat(5) + "+" + "-".repeat(49) + "+");
	}
}
