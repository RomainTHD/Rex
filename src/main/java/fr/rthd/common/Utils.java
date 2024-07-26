package fr.rthd.common;

public abstract class Utils {
	public static String u8ToString(int value, boolean usePrefix) {
		if (usePrefix) {
			return String.format("0x%1$02X", value);
		} else {
			return String.format("%1$02X", value);
		}
	}

	public static String u8ToString(int value) {
		return u8ToString(value, true);
	}

	public static String u16ToString(int value) {
		return String.format("0x%1$04X", value);
	}

	public static String u32ToString(long value) {
		return String.format("0x%1$08X", value);
	}
}
