package fr.rthd.types.pe;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Set;

@Builder
@Value
@ToString
public class Section {
	/**
	 * 8-byte, null-padded UTF-8 encoded string
	 */
	String name;
	/**
	 * Total size of the section when loaded into memory
	 */
	long virtualSize;
	/**
	 * Address of the first byte of the section relative to the image base when the section is loaded into memory
	 */
	long virtualAddress;
	/**
	 * Size of the initialized data on disk.
	 * This must be a multiple of fileAlignment.
	 * Can be greater than virtualSize
	 */
	long rawDataSize;
	/**
	 * File pointer to the first page of the section within the COFF file.
	 * Must be a multiple of fileAlignment
	 */
	long rawDataPtr;
	/**
	 * File pointer to the beginning of relocation entries for the section
	 */
	long relocationsPtr;
	/**
	 * File pointer to the beginning of line-number entries for the section.
	 * Set to zero if there are no COFF line numbers
	 * @deprecated
	 */
	long lineNumberPtr;
	/**
	 * Number of relocation entries for the section.
	 * Should be set to zero for images
	 */
	int relocationsCount;
	/**
	 * Number of line-number entries for the section
	 * @deprecated
	 */
	int lineNumberCount;
	/**
	 * Flags
	 */
	Set<SectionCharacteristicsFlags> characteristics;
}
