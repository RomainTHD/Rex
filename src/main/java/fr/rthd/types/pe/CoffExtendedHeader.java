package fr.rthd.types.pe;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@ToString
public class CoffExtendedHeader {
	@ToString.Exclude
	CoffHeader coffHeader;
	/**
	 * PE32 or PE32+
	 */
	boolean is64bitAddressSpace;
	/**
	 * Major and minor linker version
	 */
	int linkerVersion;
	/**
	 * Size of the code (text) section
	 */
	long textSize;
	/**
	 * Size of the initialized data section
	 */
	long dataSize;
	/**
	 * Size of the uninitialized data (BSS) section
	 */
	long bssSize;
	/**
	 * Address of the entry point relative to the image base when the executable file is loaded into memory
	 */
	long entryPointAddr;
	/**
	 * Address relative to the image base of the beginning-of-code section when it is loaded into memory
	 */
	long textBase;
	/**
	 * Address relative to the image base of the beginning-of-data section when it is loaded into memory
	 * TODO: PE32 only
	 */
	long dataBase;

	// NT-specific fields

	/**
	 * Preferred address of the first byte of image when loaded into memory
	 */
	long imageBase;
	/**
	 * Alignment (in bytes) of sections when they are loaded into memory
	 * TODO: must be greater than or equal to fileAlignment
	 */
	long sectionAlignment;
	/**
	 * Alignment factor (in bytes) that is used to align the raw data of sections in the image file
	 */
	long fileAlignment;
	long osVersion;
	long imageVersion;
	long subsystemVersion;
	/**
	 * Reserved
	 */
	long win32VersionValue;
	/**
	 * Size (in bytes) of the image, including all headers, as the image is loaded in memory
	 * TODO: must be a multiple of sectionAlignment
	 */
	long imageSize;
	/**
	 * Combined size of an MS-DOS stub, PE header, and section headers
	 * TODO: must be rounded up to a multiple of fileAlignment
	 */
	long headersSize;
	long checkSum;
	/**
	 * Subsystem required to run this file
	 */
	WindowsSubsystem subsystem;
	int dllCharacteristics;
	/**
	 * Size of the stack to reserve. Only stackSizeToCommit is committed,
	 * the rest is made available one page at a time until the reserve size is reached
	 */
	long stackSizeToReserve;
	/**
	 * Size of the stack to commit
	 */
	long stackSizeToCommit;
	/**
	 * Size of the heap to reserve. Only heapSizeToCommit is committed,
	 * the rest is made available one page at a time until the reserve size is reached
	 */
	long heapSizeToReserve;
	/**
	 * Size of the heap to commit
	 */
	long heapSizeToCommit;
	/**
	 * Reserved
	 */
	long loaderFlags;
	/**
	 * Number of data-directory entries in the remainder of the optional header.
	 * Each describes a location and a size
	 */
	long rvaCount;
}
