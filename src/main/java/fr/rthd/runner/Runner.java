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
	private Registers registers;
	private Disassembler disassembler;

	public Runner(PeFile peFile) {
		this.peFile = peFile;
	}

	public void run() {
		loadVirtualSpace();
		loadRegisters();
		disassembler = new Disassembler(virtualSpace, registers);
		jumpAt((int) this.peFile.getHeader().getCoffExtendedHeader().getEntryPointAddr());
		virtualSpace.dump();

		logger.info("Starting program execution");
		boolean canContinue;
		do {
			canContinue = disassembler.step();
		} while (canContinue);
		logger.info("Program exited with exit code " + registers.get(registers.EAX));
		registers.dump();
	}

	private void loadVirtualSpace() {
		// FIXME: what value to use?
		virtualSpace = new LittleEndianDataManager(new int[0x100_0000]);
		peFile
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

	private void loadRegisters() {
		registers = new Registers();
		// FIXME: should differentiate between reserve and commit
		registers.set(
			registers.ESP,
			peFile.getHeader().getCoffExtendedHeader().getStackSizeToReserve()
				+ peFile.getHeader().getCoffExtendedHeader().getImageBase()
		);
	}

	private void writeU8(int value) {
		// logger.debug(String.format("Writing %s", Utils.u8ToString(value)));
		virtualSpace.writeU8(value);
	}

	private void jumpAt(int newPc) {
		logger.debug("Jumped at @" + Utils.u32ToString(newPc));
		virtualSpace.jumpAt(newPc);
	}

	private RuntimeException fail(ExitCode exitCode, String reason) {
		return FailureManager.fail(Runner.class, exitCode, reason);
	}
}
