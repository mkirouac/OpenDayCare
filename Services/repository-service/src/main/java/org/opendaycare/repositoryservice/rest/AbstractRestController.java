package org.opendaycare.repositoryservice.rest;

import java.time.ZonedDateTime;

import org.opendaycare.repositoryservice.model.IdentifiableModel;
import org.opendaycare.repositoryservice.service.RepositoryService;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceException;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceNonRetryableException;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceRetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractRestController<TModel extends IdentifiableModel<TId>, TId> {


	private final RepositoryService<TModel, TId> service;
	
	public AbstractRestController(RepositoryService<TModel, TId> service) {
		this.service = service;
	}
	
	@GetMapping
	public Flux<TModel> getAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<TModel>> getById(@PathVariable TId id) {

		return service.findById(id).map(model -> ResponseEntity.ok(model))
				.defaultIfEmpty(ResponseEntity.notFound().build())
				;

	}

	@PostMapping
	public Mono<ResponseEntity<TModel>> create(@RequestBody TModel model) {
		return service.create(model)
				.map(m -> new ResponseEntity<>(m, HttpStatus.CREATED)  );
	}
	
	@PutMapping
	public Mono<ResponseEntity<TModel>> update(@RequestBody TModel model) {
		return service.update(model)
				.map(m -> ResponseEntity.ok(m))
				;
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable TId id) {
		return service.delete(id)
				.map(v -> ResponseEntity.ok(v));
	}


//	@ExceptionHandler
//	public Mono<ResponseEntity<ErrorResponse>> handleError(RepositoryServiceException exception, ServerWebExchange exchange) {
//
//		log.error("Request failed due to a RepositoryServiceException", exception);
//		
//		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
//		if(exception instanceof RepositoryServiceRetryableException) {
//			status = HttpStatus.INTERNAL_SERVER_ERROR;
//			
//		} else if(exception instanceof RepositoryServiceNonRetryableException) { 
//			status = HttpStatus.BAD_REQUEST;
//		}
//		//Sample error message when serialized to json:
//
//		return Mono.just(new ResponseEntity<ErrorResponse>(new ErrorResponse(
//				ZonedDateTime.now(), 
//				exchange.getRequest().getPath().value(), 
//				status.value(), 
//				status.getReasonPhrase(), 
//				exception.getMessage(), 
//				exchange.getRequest().getId()
//			), status));
//		
//	}
//
//	@ExceptionHandler
//	public Mono<ResponseEntity<ErrorResponse>> handleError(Exception exception, ServerWebExchange exchange) {
//
//		log.error("Request failed due to an unknown exception", exception);
//		
//		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
//		
//		return Mono.just(new ResponseEntity<ErrorResponse>(new ErrorResponse(
//				ZonedDateTime.now(), 
//				exchange.getRequest().getPath().value(), 
//				status.value(), 
//				status.getReasonPhrase(), 
//				exception.getMessage(), 
//				exchange.getRequest().getId()
//			), status));
//	}
//	
}
