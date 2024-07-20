package fr.rthd.common;

public enum ExitCode {
	OK(0),
	UnknownException(1),
	InvalidState(1),
	InvalidCliArgument(2),
	FileNotFound(3),
	InvalidFile(4);

	private final int _value;

	ExitCode(int value) {
		this._value = value;
	}

	public int getValue() {
		return _value;
	}
}
