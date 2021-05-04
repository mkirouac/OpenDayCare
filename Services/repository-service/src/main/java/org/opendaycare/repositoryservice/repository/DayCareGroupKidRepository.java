package org.opendaycare.repositoryservice.repository;

import org.opendaycare.repositoryservice.model.DayCareGroupKid;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.util.function.Tuple2;

public interface DayCareGroupKidRepository extends ReactiveCrudRepository<DayCareGroupKid, Tuple2<Long, Long>> {

}
