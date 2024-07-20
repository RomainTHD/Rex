package fr.rthd.common;

public abstract class FailureManager {
	private static final Logger logger = new Logger(FailureManager.class);
	public static RuntimeException fail(ExitCode code) {
		logger.error("Error: " + code.toString());
		System.exit(code.getValue());
		return new RuntimeException();
	}

	public static RuntimeException fail(ExitCode code, String reason) {
		logger.error(reason);
		return fail(code);
	}

	public static RuntimeException fail(ExitCode code, Exception e) {
		e.printStackTrace();
		return fail(code);
	}

	public static void success() {
		logger.info("OK");
		System.exit(ExitCode.OK.getValue());
	}
}
