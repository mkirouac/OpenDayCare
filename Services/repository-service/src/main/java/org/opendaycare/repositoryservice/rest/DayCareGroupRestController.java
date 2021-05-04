package org.opendaycare.repositoryservice.rest;

import org.opendaycare.repositoryservice.model.DayCareGroup;
import org.opendaycare.repositoryservice.model.Employee;
import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.service.daycaregroup.DayCareGroupService;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceNonRetryableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/daycaregroup")
public class DayCareGroupRestController extends AbstractRestController<DayCareGroup, Long> {

	private final DayCareGroupService service;
	
	
	@Autowired
	public DayCareGroupRestController(DayCareGroupService service) {
		super(service);
		this.service = service;
	}

	@GetMapping("{idGroup}/kids")
	public Flux<Kid> getGroupKids(@PathVariable Long idGroup) {
		
		
		
		return service.findById(idGroup)
				.switchIfEmpty(groupDoesNotExistsError(idGroup))
				.flatMapMany(group -> service.findGroupKids(group));
		
	}
	
	@GetMapping("{idGroup}/employees")
	public Flux<Employee> getGroupEmployees(@PathVariable Long idGroup) {
		return service.findById(idGroup)
				//.switchIfEmpty(Mono.error(groupDoesNotExistsError(idGroup)))
				.switchIfEmpty(groupDoesNotExistsError(idGroup))
				.flatMapMany(group -> service.findGroupEmployees(group));
	}
	
	@PutMapping("{idGroup}/kids/{idKid}") 
	public Mono<ResponseEntity<String>> assignGroupToKid(@PathVariable Long idGroup, @PathVariable Long idKid) {
		return service.assignGroupToKid(idGroup, idKid)
			.then(Mono.just(new ResponseEntity<String>("", HttpStatus.CREATED)))
			;
	}
	
	@PutMapping("{idGroup}/employees/{idEmployee}") 
	public Mono<ResponseEntity<Void>> assignGroupToEmployee(@PathVariable Long idGroup, @PathVariable Long idEmployee) {
		return service.assignGroupToEmployee(idGroup, idEmployee)
			.then(Mono.just(new ResponseEntity<Void>(HttpStatus.CREATED)));
	}
	
	
	@DeleteMapping("{idGroup}/kids/{idKid}") 
	public Mono<ResponseEntity<String>> unassignGroupFromKid(@PathVariable Long idGroup, @PathVariable Long idKid) {
		return service.unassignGroupFromKid(idGroup, idKid)
			.then(Mono.just(new ResponseEntity<String>("", HttpStatus.OK)))
			;
	}
	
	@DeleteMapping("{idGroup}/employees/{idEmployee}") 
	public Mono<ResponseEntity<Void>> unassignGroupFromEmployee(@PathVariable Long idGroup, @PathVariable Long idEmployee) {
		return service.unassignGroupFromEmployee(idGroup, idEmployee)
			.then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
	}
	
	
	

	private <T> Mono<T>  groupDoesNotExistsError(Long idGroup) {
		return Mono.error(new  RepositoryServiceNonRetryableException(String.format("The group with id %s doesn't exists.", idGroup)));
	}
	
}
