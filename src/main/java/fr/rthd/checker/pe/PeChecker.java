package fr.rthd.checker.pe;

import fr.rthd.checker.LittleEndianChecker;
import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.types.pe.CoffCharacteristicsFlags;
import fr.rthd.types.pe.CoffHeader;
import fr.rthd.types.pe.MachineType;

import java.util.List;

/**
 * Portable Executable format
 * FIXME: Currently only supports x86
 */
public class PeChecker extends LittleEndianChecker {
	private static final Logger logger = new Logger(PeChecker.class);

	public PeChecker(List<Byte> bytes) {
		super(bytes);
	}

	@Override
	public void check() {
		checkDosHeader();
		checkDosStub();
		var coffHeader = checkCoffHeader();
		checkNtHeaders();
		checkSectionTable();
		checkSections();
	}

	private void checkDosHeader() {
		if (nextU16() != 0x5a4d) {
			throw FailureManager.fail(
				PeChecker.class,
				ExitCode.InvalidFile,
				"Not a DOS executable: magic number not found"
			);
		}

		skipAt(0x3c);
		var peHeaderStart = nextU32();
		skipAt(peHeaderStart);
	}

	private void checkDosStub() {
		// Never checked, @0x3c will skip directly to NT headers
	}

	private CoffHeader checkCoffHeader() {
		if (nextU32() != 0x4550) {
			throw FailureManager.fail(PeChecker.class, ExitCode.InvalidFile, "Not a PE image: magic number not found");
		}

		var coffHeader = CoffHeader
			.builder()
			.machine(MachineType.fromValue(nextU16()))
			.numberOfSections(nextU16())
			.timeDateStamp(nextU32())
			.symbolTablePtr(nextU32())
			.symbolsCount(nextU32())
			.optHeaderSize(nextU16())
			.characteristics(nextU16())
			.build();
		logger.debug(coffHeader.toString());

		if ((coffHeader.getCharacteristics() & CoffCharacteristicsFlags.ExecutableImage.getValue()) == 0) {
			throw FailureManager.fail(PeChecker.class, ExitCode.InvalidFile, "File is not executable");
		}

		if ((coffHeader.getCharacteristics() & CoffCharacteristicsFlags.Machine32Bit.getValue()) == 0) {
			// FIXME: is it really 16 bits exe?
			throw FailureManager.fail(
				PeChecker.class,
				ExitCode.Unsupported,
				"16 bits executables are not supported"
			);
		}

		if ((coffHeader.getCharacteristics() & CoffCharacteristicsFlags.DLL.getValue()) != 0) {
			throw FailureManager.fail(PeChecker.class, ExitCode.Unsupported, "DLLs are not supported yet");
		}

		return coffHeader;
	}

	private void checkNtHeaders() {

	}

	private void checkSectionTable() {
	}

	private void checkSections() {
	}
}
