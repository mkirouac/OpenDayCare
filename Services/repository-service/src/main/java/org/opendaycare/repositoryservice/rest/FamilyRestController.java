package org.opendaycare.repositoryservice.rest;

import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.model.Tutor;
import org.opendaycare.repositoryservice.service.family.FamilyService;
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
@RequestMapping(path = "/family")
public class FamilyRestController  {

	private final FamilyService service;
	
	@Autowired
	public FamilyRestController(
			FamilyService service) {
		this.service = service;
	}

	@GetMapping("tutor/{idTutor}/kid")
	public Flux<Kid> getTutorKids(@PathVariable Long idTutor) {
		
		return service.findTutorKids(idTutor);
		
	}
	
	@GetMapping("kid/{idKid}/tutor")
	public Flux<Tutor> getKidsTutors(@PathVariable Long idKid) {
		return service.findKidTutors(idKid);
	}
	
	@PutMapping("kid/{idKid}/tutor/{idTutor}") 
	public Mono<ResponseEntity<String>> assignTutorToKid(@PathVariable Long idTutor, @PathVariable Long idKid) {
		return service.assignTutorToKid(idTutor, idKid)
			.then(Mono.just(new ResponseEntity<String>("", HttpStatus.CREATED)))
			;
	}

	@PutMapping("tutor/{idTutor}/kid/{idKid}") 
	public Mono<ResponseEntity<String>> assignKidToTutor(@PathVariable Long idTutor, @PathVariable Long idKid) {
		return service.assignTutorToKid(idTutor, idKid)
			.then(Mono.just(new ResponseEntity<String>("", HttpStatus.CREATED)))
			;
	}

	

	@DeleteMapping("kid/{idKid}/tutor/{idTutor}") 
	public Mono<ResponseEntity<String>> unassignTutorFromKid(@PathVariable Long idTutor, @PathVariable Long idKid) {
		return service.unassignTutorFromKid(idTutor, idKid)
			.then(Mono.just(new ResponseEntity<String>("", HttpStatus.OK)))
			;
	}
	
	@DeleteMapping("tutor/{idTutor}/kid/{idKid}") 
	public Mono<ResponseEntity<String>> unassignKidFromTutor(@PathVariable Long idTutor, @PathVariable Long idKid) {
		return service.unassignTutorFromKid(idTutor, idKid)
			.then(Mono.just(new ResponseEntity<String>("", HttpStatus.OK)))
			;
	}
	
}
