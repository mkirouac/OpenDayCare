package org.opendaycare.repositoryservice.repository;

import org.opendaycare.repositoryservice.model.DayCareGroupEmployee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.util.function.Tuple2;

public interface DayCareGroupEmployeeRepository extends ReactiveCrudRepository<DayCareGroupEmployee, Tuple2<Long, Long>> {

}
