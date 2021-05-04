package org.opendaycare.repositoryservice.repository;

import org.opendaycare.repositoryservice.model.Family;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.util.function.Tuple2;

public interface FamilyRepository extends ReactiveCrudRepository<Family, Tuple2<Long, Long>> {

}
