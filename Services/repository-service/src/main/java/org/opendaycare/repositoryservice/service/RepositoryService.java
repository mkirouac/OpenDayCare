package org.opendaycare.repositoryservice.service;

import org.opendaycare.repositoryservice.model.IdentifiableModel;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RepositoryService<TModel extends IdentifiableModel<TId>, TId> {

	Flux<TModel> findAll() ;
	
	Mono<TModel> findById(TId id);
	
	public Mono<TModel> create(TModel model);
	
	public Mono<TModel> update(TModel model);
	
	public Mono<Void> delete(TId id);
	
}
