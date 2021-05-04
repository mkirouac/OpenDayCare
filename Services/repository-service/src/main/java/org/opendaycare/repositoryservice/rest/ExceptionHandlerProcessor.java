package org.opendaycare.repositoryservice.rest;

import java.time.ZonedDateTime;

import org.opendaycare.repositoryservice.service.exception.RepositoryServiceException;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceNonRetryableException;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceRetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@ControllerAdvice
@Slf4j
public class ExceptionHandlerProcessor {

	@ExceptionHandler
	public Mono<ResponseEntity<ErrorResponse>> handleError(RepositoryServiceException exception, ServerWebExchange exchange) {

		log.error("Request failed due to a RepositoryServiceException", exception);
		
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		if(exception instanceof RepositoryServiceRetryableException) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			
		} else if(exception instanceof RepositoryServiceNonRetryableException) { 
			status = HttpStatus.BAD_REQUEST;
		}
		//Sample error message when serialized to json:

		return Mono.just(new ResponseEntity<ErrorResponse>(new ErrorResponse(
				ZonedDateTime.now(), 
				exchange.getRequest().getPath().value(), 
				status.value(), 
				status.getReasonPhrase(), 
				exception.getMessage(), 
				exchange.getRequest().getId()
			), status));
		
	}

	@ExceptionHandler
	public Mono<ResponseEntity<ErrorResponse>> handleError(Exception exception, ServerWebExchange exchange) {

		log.error("Request failed due to an unknown exception", exception);
		
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		
		return Mono.just(new ResponseEntity<ErrorResponse>(new ErrorResponse(
				ZonedDateTime.now(), 
				exchange.getRequest().getPath().value(), 
				status.value(), 
				status.getReasonPhrase(), 
				exception.getMessage(), 
				exchange.getRequest().getId()
			), status));
	}
	

}
