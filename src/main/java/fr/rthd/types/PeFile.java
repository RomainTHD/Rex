package fr.rthd.types;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@Builder
@Value
@ToString
public class PeFile {
	PeHeader header;
	List<PeSection> sections;
}
