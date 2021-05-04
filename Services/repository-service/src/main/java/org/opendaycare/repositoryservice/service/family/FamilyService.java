package org.opendaycare.repositoryservice.service.family;

import org.opendaycare.repositoryservice.model.Family;
import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.model.Tutor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FamilyService {
	Mono<Family> assignTutorToKid(Tutor tutor, Kid kid);
	
	Mono<Family> assignTutorToKid(Long idTutor, Long idKid);
	
	Mono<Void> unassignTutorFromKid(Tutor tutor, Kid kid);
	
	Mono<Void> unassignTutorFromKid(Long idTutor, Long idKid);
	
	Flux<Tutor> findKidTutors(Kid kid);
	
	Flux<Tutor> findKidTutors(Long idKid);
	
	Flux<Kid> findTutorKids(Tutor kid);
	
	Flux<Kid> findTutorKids(Long idTutor);
}
