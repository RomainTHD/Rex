package fr.rthd.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@AllArgsConstructor
@Builder
@Value
@ToString
public class Pair<L, R> {
	L left;
	R right;
}
