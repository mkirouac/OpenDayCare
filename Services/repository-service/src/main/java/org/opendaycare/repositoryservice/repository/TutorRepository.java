package org.opendaycare.repositoryservice.repository;

import org.opendaycare.repositoryservice.model.Tutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TutorRepository extends ReactiveCrudRepository<Tutor, Long>{

}
