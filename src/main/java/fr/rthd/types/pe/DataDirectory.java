package fr.rthd.types.pe;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@ToString
public class DataDirectory {
	DataDirectoryFieldName fieldName;
	/**
	 * Virtual address, aka RVA
	 */
	long addr;
	long size;
}
