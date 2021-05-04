package org.opendaycare.repositoryservice.rest;

import org.opendaycare.repositoryservice.model.Employee;
import org.opendaycare.repositoryservice.service.employee.EmployeeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/employee")
public class EmployeeRestController extends AbstractRestController<Employee, Long> {

	public EmployeeRestController(EmployeeService service) {
		super(service);
	}
	
	
}
