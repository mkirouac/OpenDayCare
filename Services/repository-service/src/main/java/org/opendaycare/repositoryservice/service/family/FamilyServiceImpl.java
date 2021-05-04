package org.opendaycare.repositoryservice.service.family;

import org.opendaycare.repositoryservice.model.Family;
import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.model.Tutor;
import org.opendaycare.repositoryservice.repository.FamilyRepository;
import org.opendaycare.repositoryservice.repository.KidsRepository;
import org.opendaycare.repositoryservice.repository.TutorRepository;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceNonRetryableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FamilyServiceImpl implements FamilyService {

	private final FamilyRepository familyRepository;
	private final TutorRepository tutorRepository;
	private final KidsRepository kidRepository;
	private final DatabaseClient databaseClient;
	private final MappingR2dbcConverter converter;
	
	public FamilyServiceImpl(
			FamilyRepository familyRepository,
			TutorRepository tutorRepository,
			KidsRepository kidRepository,
			DatabaseClient databaseClient,
			MappingR2dbcConverter converter) {
		this.familyRepository = familyRepository;
		this.tutorRepository = tutorRepository;
		this.kidRepository = kidRepository;
		this.databaseClient = databaseClient;
		this.converter = converter;
	}

	@Override
	public Mono<Family> assignTutorToKid(Tutor tutor, Kid kid) {
		return assignTutorToKid(tutor.getId(), kid.getId());
	}

	@Override
	public Mono<Family> assignTutorToKid(Long idTutor, Long idKid) {
		Family family = new Family(idKid, idTutor);

		return tutorRepository.existsById(idTutor).filter(exists -> exists).switchIfEmpty(tutorDoesNotExistsError(idTutor))
				.then(kidRepository.existsById(idKid)).filter(exists -> exists).switchIfEmpty(kidDoesNotExistsError(idKid))
				.then(familyRepository.save(family)).onErrorMap(DataIntegrityViolationException.class,
						e -> familyAssociateExistsError(e, idKid, idTutor));
		
	}

	@Override
	public Mono<Void> unassignTutorFromKid(Tutor tutor, Kid kid) {
		return unassignTutorFromKid(tutor.getId(), kid.getId());
	}

	@Override
	public Mono<Void> unassignTutorFromKid(Long idTutor, Long idKid) {
		return databaseClient.sql("DELETE from Family WHERE idTutor = :idTutor AND idKid = :idKid")
				.bind("idTutor", idTutor)
				.bind("idKid", idKid)
				.fetch().rowsUpdated()//TODO Throw if ==0
				.then(Mono.empty());
	}

	@Override
	public Flux<Tutor> findKidTutors(Kid kid) {
		return findKidTutors(kid.getId());
	}

	@Override
	public Flux<Tutor> findKidTutors(Long idKid) {
		return databaseClient.sql("SELECT t.* FROM Tutor t INNER JOIN Family f on f.idTutor = t.idTutor WHERE f.idKid = :idKid")
				.bind("idKid", idKid)
				.map((row, metadata) -> converter.read(Tutor.class, row, metadata)).all();
	}
	
	@Override
	public Flux<Kid> findTutorKids(Tutor tutor) {
		return findTutorKids(tutor.getId());
	}

	@Override
	public Flux<Kid> findTutorKids(Long idTutor) {
		return databaseClient.sql("SELECT k.* FROM Kid k INNER JOIN Family f on f.idKid = k.idKid WHERE f.idTutor = :idTutor")
				.bind("idTutor", idTutor)
				.map((row, metadata) -> converter.read(Kid.class, row, metadata)).all();
	}

	
	private <T> Mono<T> tutorDoesNotExistsError(Long idTutor) {
		return Mono.error(
				new RepositoryServiceNonRetryableException(String.format("The tutor %s doesn't exists", idTutor)));
	}
	
	private <T> Mono<T> kidDoesNotExistsError(Long idKid) {
		return Mono.error(
				new RepositoryServiceNonRetryableException(String.format("The kid %s doesn't exists", idKid)));
	}
	
	private RepositoryServiceNonRetryableException familyAssociateExistsError(
			DataIntegrityViolationException cause, Long idKid, Long tutor) {
		return new RepositoryServiceNonRetryableException(
				String.format("The kid '%s' is already associated with the tutor '%s'", idKid, tutor),
				cause);
	}
	
	
}
