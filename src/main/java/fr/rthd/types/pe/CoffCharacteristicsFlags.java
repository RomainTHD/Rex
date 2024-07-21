package fr.rthd.types.pe;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoffCharacteristicsFlags {
	/**
	 * File does not contain base relocations and must be loaded at its preferred base address
	 */
	RelocsStripped(0x0001),
	/**
	 * File is valid and can be run
	 */
	ExecutableImage(0x0002),
	/**
	 * @deprecated
	 */
	NumsStripped(0x0004),
	/**
	 * @deprecated
	 */
	SymsStripped(0x0008),
	/**
	 * @deprecated
	 */
	AggressiveWsTrim(0x0010),
	/**
	 * Application can handle > 2GB addresses
	 */
	LargeAddressAware(0x0020),
	/**
	 * @deprecated
	 */
	BytesReversedLo(0x0080),
	/**
	 * Machine is based on a 32-bit-word architecture
	 */
	Machine32Bit(0x0100),
	/**
	 * Debugging information is removed from the file
	 */
	DebugStripped(0x0200),
	/**
	 * If the image is on removable media, fully load it and copy it to the swap file
	 */
	RemovableRunFromSwap(0x0400),
	/**
	 * If the image is on network media, fully load it and copy it to the swap file
	 */
	NetRunFromSwap(0x0800),
	/**
	 * System file, not a user program
	 */
	System(0x1000),
	/**
	 * File is a DLL
	 */
	DLL(0x2000),
	/**
	 * File should be run only on a uniprocessor machine
	 */
	UpSystemOnly(0x4000),
	/**
	 * @deprecated
	 */
	BytesReversedHi(0x8000);

	private final long value;
}
