package org.opendaycare.repositoryservice.service;

import org.opendaycare.repositoryservice.model.IdentifiableModel;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceNonRetryableException;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractRepositoryService<TModel extends IdentifiableModel<TId>, TId> implements RepositoryService<TModel, TId>{

	//Really good SE answer regarding exception handling: https://stackoverflow.com/questions/53595420/correct-way-of-throwing-exceptions-with-reactor
	
	private final ReactiveCrudRepository<TModel, TId> repository;
	private final String modelName;
	
	public AbstractRepositoryService(ReactiveCrudRepository<TModel, TId> repository, String modelName) {
		this.repository = repository;
		this.modelName = modelName;
	}
	
	@Override
	public Flux<TModel> findAll() {
		return repository.findAll();
	}

	@Override
	public Mono<TModel> findById(@NonNull TId id) {
		
		return repository.findById(id);
	}
	
	@Override
	public Mono<TModel> create(@NonNull TModel model) {
		if(model.getId() != null) {
			return Mono.error(new RepositoryServiceNonRetryableException("Request to create a " + modelName + " must not have an id defined. To update a model, use the update method."));
		}
		
		return repository
				.save(model);
	}
	
	@Override
	public Mono<TModel> update(@NonNull TModel model) {
		return repository
				.findById(model.getId())
				.switchIfEmpty(Mono.error(new RepositoryServiceNonRetryableException(modelName + " with id " + model.getId() + " does not exists.")))
				.then(repository.save(model));
	}
	
	@Override
	public Mono<Void> delete(@NonNull TId id) {
		return repository
				.findById(id)
				.switchIfEmpty(Mono.error(new RepositoryServiceNonRetryableException(modelName + " with id " + id + " does not exists.")))
				.then(repository.deleteById(id));
		
	}
	
}
