package fr.rthd.checker;

import fr.rthd.types.ExecutableFormat;
import fr.rthd.checker.pe.PeLoader;
import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.NotImplementedException;

import java.util.List;

public class LoaderFactory {
	public static Loader getInstance(ExecutableFormat format, List<Byte> bytes) {
		return switch (format) {
			case PE -> new PeLoader(bytes);
			case ELF -> throw new NotImplementedException();
			default -> throw FailureManager.fail(LoaderFactory.class, ExitCode.InvalidState);
		};
	}
}
