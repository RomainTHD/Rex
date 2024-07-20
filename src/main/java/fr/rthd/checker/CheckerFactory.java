package fr.rthd.checker;

import fr.rthd.common.ExecutableFormat;
import fr.rthd.checker.pe.PeChecker;
import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.NotImplementedException;

import java.util.List;

public class CheckerFactory {
	public static Checker getInstance(ExecutableFormat format, List<Byte> bytes) {
		return switch (format) {
			case PE -> new PeChecker(bytes);
			case ELF -> throw new NotImplementedException();
			default -> throw FailureManager.fail(ExitCode.InvalidState);
		};
	}
}
