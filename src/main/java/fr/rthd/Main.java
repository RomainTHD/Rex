package fr.rthd;

import fr.rthd.checker.LoaderFactory;
import fr.rthd.types.ExecutableFormat;
import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.io.BinaryReader;

public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			// TODO: handle args properly
			throw FailureManager.fail(Main.class, ExitCode.InvalidCliArgument);
		}

		if (args[0].equals("-i")) {
			// TODO: move somewhere else
			var content = new BinaryReader().readFileFromPath(args[1]);
			LoaderFactory.getInstance(ExecutableFormat.PE, content).load();
		}

		FailureManager.success();
	}
}
