package org.opendaycare.repositoryservice.repository;

import org.opendaycare.repositoryservice.model.Kid;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface KidsRepository extends ReactiveCrudRepository<Kid, Long>{

}
