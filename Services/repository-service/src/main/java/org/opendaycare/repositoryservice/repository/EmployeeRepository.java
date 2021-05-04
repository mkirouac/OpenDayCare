package org.opendaycare.repositoryservice.repository;

import org.opendaycare.repositoryservice.model.Employee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EmployeeRepository extends ReactiveCrudRepository<Employee, Long> {

}
