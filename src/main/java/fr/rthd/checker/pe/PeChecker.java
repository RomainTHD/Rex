package fr.rthd.checker.pe;

import fr.rthd.checker.LittleEndianChecker;
import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;

import java.util.List;

/**
 * Portable Executable format
 * FIXME: Currently only supports x86
 */
public class PeChecker extends LittleEndianChecker {

	public PeChecker(List<Byte> bytes) {
		super(bytes);
	}

	@Override
	public void check() {
		checkDosHeader();
		checkDosStub();
		checkNtHeaders();
		checkSectionTable();
		checkSections();
	}

	private void checkDosHeader() {
		if (nextU16() != 0x5a4d) {
			throw FailureManager.fail(ExitCode.InvalidFile, "Not a DOS executable: signature 0x5a4d not found");
		}

		skipAt(0x3c);
		var peHeaderStart = nextU32();
		skipAt(peHeaderStart);
	}

	private void checkDosStub() {
		// Never executed, @0x3c will skip directly to NT headers
	}

	private void checkNtHeaders() {

	}

	private void checkSectionTable() {
	}

	private void checkSections() {
	}
}
