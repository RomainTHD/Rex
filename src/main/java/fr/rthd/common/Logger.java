package fr.rthd.common;

public class Logger {
	public enum Level {
		Debug,
		Info,
		Warn,
		Error,
	}

	private final String _className;
	private final Level _minLevel;

	public Logger(Class<?> cls) {
		this(cls, Level.Debug);
	}

	public Logger(Class<?> cls, Level minLevel) {
		this._className = cls.getName();
		this._minLevel = minLevel;
	}

	public void debug(String msg) {
		if (Level.Debug.ordinal() >= _minLevel.ordinal()) {
			System.out.println("[D] " + msg);
		}
	}

	public void log(String msg) {
		System.out.println("[L] " + msg);
	}

	public void info(String msg) {
		System.out.println("[I] " + msg);
	}

	public void warn(String msg) {
		System.err.println("[W] " + msg);
		System.err.println("\t@" + _className);
	}

	public void error(String msg) {
		System.err.println("[E] " + msg);
	}
}
