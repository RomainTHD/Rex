package fr.rthd.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExitCode {
	OK(0),
	UnknownException(1),
	InvalidState(1),
	InvalidCliArgument(2),
	FileNotFound(3),
	InvalidFile(4),
	Unsupported(5),
	IllegalOperation(6);

	private final int value;
}
