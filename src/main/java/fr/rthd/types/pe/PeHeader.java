package fr.rthd.types.pe;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.LinkedHashMap;

@Builder
@Value
@ToString
public class PeHeader {
	@ToString.Exclude
	CoffExtendedHeader coffExtendedHeader;
	LinkedHashMap<DataDirectoryFieldName, DataDirectory> dataDirectories;
}
