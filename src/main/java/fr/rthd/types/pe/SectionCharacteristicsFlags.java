package fr.rthd.types.pe;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum SectionCharacteristicsFlags {
	/**
	 * The section should not be padded to the next boundary
	 * @deprecated
	 */
	TypeNoPad(0x00000008),
	ContainsCode(0x00000020),
	ContainsInitializedData(0x00000040),
	ContainsUninitializedData(0x00000080),
	/**
	 * The section contains comments or other information.
	 * The .drectve section has this type
	 */
	ContainsComments(0x00000200),
	/**
	 * The section will not become part of the image
	 */
	Remove(0x00000800),
	/**
	 * The section contains COMDAT data
	 */
	Comdat(0x00001000),
	/**
	 * The section contains data referenced through the global pointer (GP)
	 */
	GlobalPointer(0x00008000),
	ExtendedRelocations(0x01000000),
	Discardable(0x02000000),
	NotCacheable(0x04000000),
	NotPageable(0x08000000),
	/**
	 * The section can be shared in memory
	 */
	SharedMemory(0x10000000),
	Executable(0x20000000),
	Readable(0x40000000),
	Writeable(0x80000000);

	private final long value;

	public static Set<SectionCharacteristicsFlags> toFlagSet(long i) {
		return Arrays
			.stream(values())
			.filter((v) -> (v.getValue() & i) != 0)
			.collect(Collectors.toUnmodifiableSet());
	}
}
