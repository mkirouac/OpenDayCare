package org.opendaycare.repositoryservice.repository;

import org.opendaycare.repositoryservice.model.DayCareGroup;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DayCareGroupRepository extends ReactiveCrudRepository<DayCareGroup, Long> {

}