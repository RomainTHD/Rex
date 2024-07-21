package fr.rthd.checker.pe;

import fr.rthd.checker.LittleEndianChecker;
import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.types.pe.CoffCharacteristicsFlags;
import fr.rthd.types.pe.CoffExtendedHeader;
import fr.rthd.types.pe.CoffHeader;
import fr.rthd.types.pe.MachineType;
import fr.rthd.types.pe.WindowsSubsystem;

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
		checkOptCoffHeader(coffHeader);
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
			.characteristics(CoffCharacteristicsFlags.toFlagSet(nextU16()))
			.build();
		logger.debug(coffHeader.toString());

		if (!coffHeader.getCharacteristics().contains(CoffCharacteristicsFlags.ExecutableImage)) {
			throw FailureManager.fail(PeChecker.class, ExitCode.InvalidFile, "File is not executable");
		}

		if (!coffHeader.getCharacteristics().contains(CoffCharacteristicsFlags.Machine32Bit)) {
			// FIXME: is it really 16 bits exe?
			throw FailureManager.fail(
				PeChecker.class,
				ExitCode.Unsupported,
				"16 bits executables are not supported"
			);
		}

		if (coffHeader.getCharacteristics().contains(CoffCharacteristicsFlags.DLL)) {
			throw FailureManager.fail(PeChecker.class, ExitCode.Unsupported, "DLLs are not supported yet");
		}

		return coffHeader;
	}

	private CoffExtendedHeader checkOptCoffHeader(CoffHeader coffHeader) {
		// TODO: Make sure the header size is correct and doesn't overwrite data

		if (coffHeader.getOptHeaderSize() == 0) {
			throw FailureManager.fail(
				PeChecker.class,
				ExitCode.Unsupported,
				"Optional COFF header is currently required"
			);
		}

		var format = nextU16();
		if (format == 0x10b) {
			// OK, PE32
		} else if (format == 0x20b) {
			throw FailureManager.fail(
				PeChecker.class,
				ExitCode.Unsupported,
				"64 bit address space is not supported yet"
			);
		} else {
			throw FailureManager.fail(
				PeChecker.class,
				ExitCode.InvalidFile,
				"Incorrect optional COFF header magic number"
			);
		}

		var extHeader = CoffExtendedHeader
			.builder()
			.coffHeader(coffHeader)
			.is64bitAddressSpace(false)
			.linkerVersion(nextU16())
			.textSize(nextU32())
			.dataSize(nextU32())
			.bssSize(nextU32())
			.entryPointAddr(nextU32())
			.textBase(nextU32())
			.dataBase(nextU32())
			.imageBase(nextU32())
			.sectionAlignment(nextU32())
			.fileAlignment(nextU32())
			.osVersion(nextU32())
			.imageVersion(nextU32())
			.subsystemVersion(nextU32())
			.win32VersionValue(nextU32())
			.imageSize(nextU32())
			.headersSize(nextU32())
			.checkSum(nextU32())
			.subsystem(WindowsSubsystem.fromValue(nextU16()))
			.dllCharacteristics(nextU16())
			.stackSizeToReserve(nextU32())
			.stackSizeToCommit(nextU32())
			.heapSizeToReserve(nextU32())
			.heapSizeToCommit(nextU32())
			.loaderFlags(nextU32())
			.rvaCount(nextU32())
			.build();
		logger.debug(extHeader.toString());

		// TODO: do all kind of checks

		return extHeader;
	}

	private void checkNtHeaders() {

	}

	private void checkSectionTable() {
	}

	private void checkSections() {
	}
}
