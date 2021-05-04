package org.opendaycare.repositoryservice.rest;

import java.time.ZonedDateTime;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ErrorResponse {
	private final ZonedDateTime date;
	private final String path;
	private final int status;
	private final String error;
	private final String message;
	private final String requestId;
}