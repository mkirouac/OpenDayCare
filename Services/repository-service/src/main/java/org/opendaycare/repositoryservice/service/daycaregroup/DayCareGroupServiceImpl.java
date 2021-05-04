package org.opendaycare.repositoryservice.service.daycaregroup;

import org.opendaycare.repositoryservice.model.DayCareGroup;
import org.opendaycare.repositoryservice.model.DayCareGroupEmployee;
import org.opendaycare.repositoryservice.model.DayCareGroupKid;
import org.opendaycare.repositoryservice.model.Employee;
import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.repository.DayCareGroupEmployeeRepository;
import org.opendaycare.repositoryservice.repository.DayCareGroupKidRepository;
import org.opendaycare.repositoryservice.repository.DayCareGroupRepository;
import org.opendaycare.repositoryservice.repository.EmployeeRepository;
import org.opendaycare.repositoryservice.repository.KidsRepository;
import org.opendaycare.repositoryservice.service.AbstractRepositoryService;
import org.opendaycare.repositoryservice.service.exception.RepositoryServiceNonRetryableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
public class DayCareGroupServiceImpl extends AbstractRepositoryService<DayCareGroup, Long>
		implements DayCareGroupService {

	private final DayCareGroupRepository groupRepository;
	private final DayCareGroupKidRepository groupKidRepository;
	private final DayCareGroupEmployeeRepository groupEmployeeRepository;
	private final EmployeeRepository employeeRepository;
	private final KidsRepository kidRepository;
	private final DatabaseClient databaseClient;
	private final MappingR2dbcConverter converter;

	@Autowired
	public DayCareGroupServiceImpl(DayCareGroupRepository groupRepository, DayCareGroupKidRepository groupKidRepository,
			DayCareGroupEmployeeRepository groupEmployeeRepository, EmployeeRepository employeeRepository,
			KidsRepository kidRepository, DatabaseClient databaseClient, MappingR2dbcConverter converter) {
		super(groupRepository, "Day Care Group");
		this.groupRepository = groupRepository;
		this.groupKidRepository = groupKidRepository;
		this.groupEmployeeRepository = groupEmployeeRepository;
		this.employeeRepository = employeeRepository;
		this.kidRepository = kidRepository;
		this.databaseClient = databaseClient;
		this.converter = converter;
	}

	@Override
	public Flux<Employee> findGroupEmployees(DayCareGroup group) {

		return findGroupEmployees(group.getId());
	}

	@Override
	public Flux<Employee> findGroupEmployees(Long idGroup) {
		return databaseClient.sql(
				"SELECT e.* from Employee e INNER JOIN DayCareGroupEmployee ge on ge.idEmployee = e.idEmployee WHERE ge.idDayCareGroup = :idGroup")
				.bind("idGroup", idGroup).map((row, metadata) -> converter.read(Employee.class, row, metadata)).all();
	}

	@Override
	public Mono<DayCareGroupEmployee> assignGroupToEmployee(DayCareGroup group, Employee employee) {

		return assignGroupToEmployee(group.getId(), employee.getId());
	}

	@Override
	public Mono<DayCareGroupEmployee> assignGroupToEmployee(Long idGroup, Long idEmployee) {
		DayCareGroupEmployee groupEmployee = new DayCareGroupEmployee(idGroup, idEmployee);

		return groupRepository.existsById(idGroup).filter(exists -> exists)
				.switchIfEmpty(groupDoesNotExistsError(idGroup)).then(employeeRepository.existsById(idEmployee))
				.filter(exists -> exists).switchIfEmpty(employeeDoesNotExistsError(idEmployee))
				.then(groupEmployeeRepository.save(groupEmployee)).onErrorMap(DataIntegrityViolationException.class,
						e -> groupEmployeeAssociateExistsError(e, idGroup, idEmployee));
	}

	@Override
	public Mono<Void> unassignGroupFromEmployee(DayCareGroup group, Employee employee) {
		return unassignGroupFromEmployee(group.getId(), employee.getId());
	}

	@Override
	public Mono<Void> unassignGroupFromEmployee(Long idGroup, Long idEmployee) {
		return databaseClient.sql("DELETE from DayCareGroupEmployee WHERE idDayCareGroup = :idGroup AND idEmployee = :idEmployee")
				.bind("idGroup", idGroup)
				.bind("idEmployee", idEmployee)
				.fetch().rowsUpdated()//TODO Throw if ==0
				.then(Mono.empty());
	}

	@Override
	public Flux<Kid> findGroupKids(DayCareGroup group) {
		return findGroupKids(group.getId());
	}

	@Override
	public Flux<Kid> findGroupKids(Long idGroup) {
		return databaseClient.sql(
				"SELECT k.* from Kid k INNER JOIN DayCareGroupKid gk on gk.idKid = k.idKid WHERE gk.idDayCareGroup = :idGroup")
				.bind("idGroup", idGroup).map((row, metadata) -> converter.read(Kid.class, row, metadata)).all();
	}

	@Override
	public Mono<DayCareGroupKid> assignGroupToKid(DayCareGroup group, Kid kid) {

		return assignGroupToKid(group.getId(), kid.getId());
	}

	@Override
	public Mono<DayCareGroupKid> assignGroupToKid(Long idGroup, Long idKid) {

		DayCareGroupKid groupKid = new DayCareGroupKid(idGroup, idKid);

		return groupRepository.existsById(idGroup).filter(exists -> exists)
				.switchIfEmpty(groupDoesNotExistsError(idGroup)).then(kidRepository.existsById(idKid))
				.filter(exists -> exists).switchIfEmpty(kidDoesNotExistsError(idKid))
				.then(groupKidRepository.save(groupKid)).onErrorMap(DataIntegrityViolationException.class,
						e -> groupKidAssociationAlreadyExistsError(e, idGroup, idKid));
	}

	@Override
	public Mono<Void> unassignGroupFromKid(DayCareGroup group, Kid kid) {
		return unassignGroupFromKid(group.getId(), kid.getId());
	}

	@Override
	public Mono<Void> unassignGroupFromKid(Long idGroup, Long idKid) {
		return databaseClient.sql("DELETE from DayCareGroupKid WHERE idDayCareGroup = :idGroup AND idKid = :idKid")
				.bind("idGroup", idGroup)
				.bind("idKid", idKid)
				.fetch().rowsUpdated()//TODO Throw if ==0
				.then(Mono.empty());
	}

	private RepositoryServiceNonRetryableException groupKidAssociationAlreadyExistsError(
			DataIntegrityViolationException cause, Long idGroup, Long idKid) {
		return new RepositoryServiceNonRetryableException(
				String.format("The kid '%s' is already associated with the group '%s'", idKid, idGroup), cause);
	}

	private RepositoryServiceNonRetryableException groupEmployeeAssociateExistsError(
			DataIntegrityViolationException cause, Long idGroup, Long idEmployee) {
		return new RepositoryServiceNonRetryableException(
				String.format("The employee '%s' is already associated with the group '%s'", idEmployee, idGroup),
				cause);
	}

	private <T> Mono<T> employeeDoesNotExistsError(Long idEmployee) {
		return Mono.error(new RepositoryServiceNonRetryableException(
				String.format("The employee %s doesn't exists", idEmployee)));
	}

	private <T> Mono<T> kidDoesNotExistsError(Long idKid) {
		return Mono
				.error(new RepositoryServiceNonRetryableException(String.format("The kid %s doesn't exists", idKid)));
	}

	private <T> Mono<T> groupDoesNotExistsError(Long idGroup) {
		return Mono.error(
				new RepositoryServiceNonRetryableException(String.format("The group %s doesn't exists", idGroup)));
	}
}
