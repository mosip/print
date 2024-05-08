package io.mosip.print.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class SignRequestDto {
	@NotBlank
	private String data;
}
