package fr.rthd.types.pe;

import fr.rthd.common.Logger;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WindowsSubsystem {
	Unknown(0),
	/**
	 * Device drivers and native Windows processes
	 */
	Native(1),
	/**
	 * GUI
	 */
	GUI(2),
	/**
	 * Console
	 */
	CUI(3),
	OS2(5),
	Posix(7);

	private static final Logger logger = new Logger(WindowsSubsystem.class);

	private final int value;

	public static WindowsSubsystem fromValue(int v) {
		for (var e : values()) {
			if (e.value == v) {
				return e;
			}
		}
		logger.warn("Unknown value " + Integer.toHexString(v) + ", will fallback to unknown");
		return Unknown;
	}
}
