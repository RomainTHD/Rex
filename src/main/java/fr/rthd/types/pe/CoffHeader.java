package fr.rthd.types.pe;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@ToString
public class CoffHeader {
	MachineType machine;
	int numberOfSections;
	long timeDateStamp;
	/**
	 * @deprecated
	 */
	long symbolTablePtr;
	/**
	 * @deprecated
	 */
	long symbolsCount;
	int optHeaderSize;
	int characteristics;
}
