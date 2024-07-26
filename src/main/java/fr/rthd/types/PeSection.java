package fr.rthd.types;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@ToString
public class PeSection {
	PeSectionHeader header;
	int[] content;
}
