package fr.rthd.runner;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.common.Utils;
import fr.rthd.io.LittleEndianDataManager;
import fr.rthd.types.PeFile;

public class Runner {
	private static final Logger logger = new Logger(Runner.class);
	private static final int PAGE_SIZE = 0x1000;
	private final PeFile peFile;
	private LittleEndianDataManager virtualMemory;
	private Registers registers;
	private Disassembler disassembler;

	public Runner(PeFile peFile) {
		this.peFile = peFile;
	}

	public void run() {
		loadVirtualSpace();
		loadRegisters();
		disassembler = new Disassembler(virtualMemory, registers);
		jumpAt((int) this.peFile.getHeader().getCoffExtendedHeader().getEntryPointAddr());
		virtualMemory.dump();

		logger.info("Starting program execution");
		boolean canContinue;
		do {
			canContinue = disassembler.step();
		} while (canContinue);
		virtualMemory.dump();
		registers.dump();
		logger.info("Program exited with exit code " + registers.get(registers.EAX));
	}

	private void loadVirtualSpace() {
		virtualMemory = new LittleEndianDataManager(
			PAGE_SIZE,
			(int) (peFile.getHeader().getCoffExtendedHeader().getImageBase() / PAGE_SIZE + 1)
		);
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
		virtualMemory.writeU8(value);
	}

	private void jumpAt(int newPc) {
		logger.debug("Jumped at @" + Utils.u32ToString(newPc));
		virtualMemory.jumpAt(newPc);
	}

	private RuntimeException fail(ExitCode exitCode, String reason) {
		return FailureManager.fail(Runner.class, exitCode, reason);
	}
}
