package fr.rthd.runner;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.common.Utils;
import fr.rthd.io.LittleEndianDataManager;
import fr.rthd.types.PeFile;

public class Runner {
	private static final Logger logger = new Logger(Runner.class);
	private final PeFile peFile;
	private LittleEndianDataManager virtualSpace;

	public Runner(PeFile peFile) {
		this.peFile = peFile;
	}

	public void run() {
		loadVirtualSpace();
		jumpAt((int) this.peFile.getHeader().getCoffExtendedHeader().getEntryPointAddr());
		dump();
	}

	private void loadVirtualSpace() {
		// FIXME: what value to use?
		this.virtualSpace = new LittleEndianDataManager(new int[0x10_000]);
		this.peFile
			.getSections()
			.forEach((section) -> {
				logger.debug(String.format(
					"Writing section %s of size %d",
					section.getHeader().getName(),
					section.getHeader().getVirtualSize()
				));
				jumpAt((int) section.getHeader().getVirtualAddress());
				for (int i = 0; i < section.getHeader().getVirtualSize(); ++i) {
					writeU8(section.getContent()[i]);
				}
			});
	}

	private void writeU8(int value) {
		logger.debug(String.format("Writing %s", Utils.u8ToString(value)));
		virtualSpace.writeU8(value);
	}

	private void jumpAt(int newPc) {
		logger.debug("Jumped at @" + Utils.u32ToString(newPc));
		this.virtualSpace.jumpAt(newPc);
	}

	private RuntimeException fail(ExitCode exitCode, String reason) {
		return FailureManager.fail(Runner.class, exitCode, reason);
	}

	private void dump() {
		logger.debug("Memory dump start");

		StringBuilder sb;

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

		for (int row = 0; row <= this.virtualSpace.size() / 16; ++row) {
			sb = new StringBuilder();
			var sum = 0;

			sb.append(String.format("%1$3XX", row));
			sb.append(" | ");

			for (int col = 0; col < 16; col++) {
				var idx = row * 16 + col;
				var cell = 0;
				if (idx < this.virtualSpace.size()) {
					cell = this.virtualSpace.readAt(idx);
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

		logger.debug("Memory dump end");
	}
}
