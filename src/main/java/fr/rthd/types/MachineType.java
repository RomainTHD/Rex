package fr.rthd.types;

import fr.rthd.common.Logger;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MachineType {
	Unknown(0),
	I386(0x14c),
	AMD64(0x8664);

	private static final Logger logger = new Logger(MachineType.class);

	private final int value;

	public static MachineType fromValue(int v) {
		for (var e : values()) {
			if (e.value == v) {
				return e;
			}
		}
		logger.warn("Unknown value " + Integer.toHexString(v) + ", will fallback to unknown");
		return Unknown;
	}
}
