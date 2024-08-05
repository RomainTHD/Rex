package fr.rthd.runner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@AllArgsConstructor
@Builder
@Value
@ToString
public class RegisterOffset {
	int register;
	int offset;
}
