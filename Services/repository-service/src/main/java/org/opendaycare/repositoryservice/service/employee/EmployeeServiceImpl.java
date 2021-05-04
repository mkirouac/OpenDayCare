package org.opendaycare.repositoryservice.service.employee;

import org.opendaycare.repositoryservice.model.Employee;
import org.opendaycare.repositoryservice.repository.EmployeeRepository;
import org.opendaycare.repositoryservice.service.AbstractRepositoryService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends AbstractRepositoryService<Employee, Long> implements EmployeeService {

	public EmployeeServiceImpl(EmployeeRepository repository) {
		super(repository, "Employee");
	}

}
