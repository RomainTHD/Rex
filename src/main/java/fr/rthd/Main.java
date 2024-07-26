package fr.rthd;

import fr.rthd.loader.Loader;
import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.io.BinaryReader;
import fr.rthd.runner.Runner;

public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			// TODO: handle args properly
			throw FailureManager.fail(Main.class, ExitCode.InvalidCliArgument);
		}

		if (args[0].equals("-i")) {
			// TODO: move somewhere else
			var content = new BinaryReader().readFileFromPath(args[1]);
			var file = new Loader(content).load();
			new Runner(file).run();
		}

		FailureManager.success();
	}
}
