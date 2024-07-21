package fr.rthd.types.pe;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Set;

@Builder
@Value
@ToString
public class CoffHeader {
	/**
	 * Identifies the type of target machine
	 */
	MachineType machine;
	/**
	 * Indicates the size of the section table, which immediately follows the headers
	 */
	int numberOfSections;
	/**
	 * UTC timestamp of when the file was created
	 */
	long timeDateStamp;
	/**
	 * File offset of the COFF symbol table
	 * @deprecated
	 */
	long symbolTablePtr;
	/**
	 * Number of entries in the symbol table
	 * @deprecated
	 */
	long symbolsCount;
	/**
	 * Size of the optional header
	 */
	int optHeaderSize;
	/**
	 * Attributes of the file
	 */
	Set<CoffCharacteristicsFlags> characteristics;
}
