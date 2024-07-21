package fr.rthd.common;

public abstract class FailureManager {
	private static final Logger logger = new Logger(FailureManager.class);

	public static RuntimeException fail(Class<?> cls, ExitCode code, String reason) {
		logger.error("Error: " + code + ": " + reason + "\n\t@" + cls.getName());
		System.exit(code.getValue());
		return new RuntimeException();
	}

	public static RuntimeException fail(Class<?> cls, ExitCode code) {
		return fail(cls, code, "");
	}

	public static RuntimeException fail(Class<?> cls, ExitCode code, Exception e) {
		e.printStackTrace();
		return fail(cls, code);
	}

	public static void success() {
		logger.info("OK");
		System.exit(ExitCode.OK.getValue());
	}
}
