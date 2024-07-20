package fr.rthd.common;

public class Logger {
	private String _className;

	public Logger(Class<?> cls) {
		_className = cls.getName();
	}

	public void debug(String msg) {
		System.out.println("[D] " + msg);
	}

	public void log(String msg) {
		System.out.println("[L] " + msg);
	}

	public void info(String msg) {
		System.out.println("[I] " + msg);
	}

	public void warn(String msg) {
		System.err.println("[W] " + msg);
	}

	public void error(String msg) {
		System.err.println("[E] " + msg);
	}
}
