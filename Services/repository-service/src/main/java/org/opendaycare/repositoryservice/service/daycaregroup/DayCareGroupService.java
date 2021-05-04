package org.opendaycare.repositoryservice.service.daycaregroup;

import org.opendaycare.repositoryservice.model.DayCareGroup;
import org.opendaycare.repositoryservice.model.DayCareGroupEmployee;
import org.opendaycare.repositoryservice.model.DayCareGroupKid;
import org.opendaycare.repositoryservice.model.Employee;
import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.service.RepositoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DayCareGroupService extends RepositoryService<DayCareGroup, Long>{
	
	Flux<Employee> findGroupEmployees(DayCareGroup group);
	
	Flux<Employee> findGroupEmployees(Long idGroup);
	
	Mono<DayCareGroupEmployee> assignGroupToEmployee(DayCareGroup group, Employee employee);
	
	Mono<DayCareGroupEmployee> assignGroupToEmployee(Long idGroup, Long idEmployee);
	
	Mono<Void> unassignGroupFromEmployee(DayCareGroup group, Employee employee);
	
	Mono<Void> unassignGroupFromEmployee(Long idGroup, Long idEmployee);
	
	Flux<Kid> findGroupKids(DayCareGroup group);
	
	Flux<Kid> findGroupKids(Long idGroup);
	
	Mono<DayCareGroupKid> assignGroupToKid(DayCareGroup group, Kid kid);
	
	Mono<DayCareGroupKid> assignGroupToKid(Long idGroup, Long idKid);
	
	Mono<Void> unassignGroupFromKid(DayCareGroup group, Kid kid);
	
	Mono<Void> unassignGroupFromKid(Long idGroup, Long idKid);
}
