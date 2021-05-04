package org.opendaycare.repositoryservice.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendaycare.repositoryservice.model.DayCareGroup;
import org.opendaycare.repositoryservice.model.DayCareGroupKid;
import org.opendaycare.repositoryservice.model.DayCareGroupEmployee;
import org.opendaycare.repositoryservice.model.Employee;
import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.model.Tutor;
import org.opendaycare.repositoryservice.repository.DayCareGroupKidRepository;
import org.opendaycare.repositoryservice.repository.DayCareGroupRepository;
import org.opendaycare.repositoryservice.repository.DayCareGroupEmployeeRepository;
import org.opendaycare.repositoryservice.repository.EmployeeRepository;
import org.opendaycare.repositoryservice.repository.KidsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class DayCareGroupControllerTest {

	private static final String RESOURCE_PATH = "/daycaregroup/";

	private static final Long NOT_EXISTS_ID = 9999999L;

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private DayCareGroupRepository repository;

	@Autowired
	private KidsRepository kidsRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private DayCareGroupKidRepository groupKidRepository;

	@Autowired
	private DayCareGroupEmployeeRepository groupTutorRepository;

	private DayCareGroup existingDayCareGroup;

	private List<Kid> groupKids = new ArrayList<>();

	private List<Employee> groupEmployees = new ArrayList<>();

	private boolean initialized = false;

	@BeforeEach
	public void setUp() {
		if (!initialized) {
			existingDayCareGroup = repository
					.save(new DayCareGroup(null, "SomeTestGroup", "A group used in integration junit")).log().block();
			for (int i = 0; i < 10; i++) {
				Kid kid = kidsRepository.save(new Kid(null, "GroupKid", "Number" + i, LocalDate.of(2021, 01, 01))).log()
						.block();
				groupKids.add(kid);
				groupKidRepository.save(new DayCareGroupKid( existingDayCareGroup.getId(), kid.getId())).log().block();
			}

			for (int i = 0; i < 3; i++) {
				Employee employee = employeeRepository
						.save(new Employee(null, "GroupTutor", "Number" + i, LocalDate.of(2021, 01, 01))).log().block();
				groupEmployees.add(employee);
				groupTutorRepository.save(new DayCareGroupEmployee(existingDayCareGroup.getId(), employee.getId())).log()
						.block();
			}
			initialized = true;
		}
	}

	@Test
	public void getAll_shouldReturnAll() {
		webTestClient.get().uri(RESOURCE_PATH).exchange().expectStatus().isOk()
				.expectBodyList(DayCareGroup.class)
				.consumeWith(response -> {
					List<DayCareGroup> groups = response.getResponseBody();
					groups.forEach(k -> {
						assertThat(k.getId()).isNotNull();
					});
				});

	}

	@Test
	public void getOneById_shouldReturnOne() {
		webTestClient.get().uri(RESOURCE_PATH + existingDayCareGroup.getId()).exchange().expectStatus().isOk()
				.expectBody(DayCareGroup.class)
				.value(k -> assertThat(k.getId()).isEqualTo(existingDayCareGroup.getId()))
				.value(k -> assertThat(k.getGroupName()).isEqualTo("SomeTestGroup"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("A group used in integration junit"));
	}

	@Test
	public void getOneById_shouldReturn404_whenNotFound() {
		webTestClient.get().uri(RESOURCE_PATH + NOT_EXISTS_ID).exchange().expectStatus().isNotFound();
	}

	@Test
	public void create_shouldCreateOne() {

		DayCareGroup group = new DayCareGroup(null, "Newly", "Created group");

		webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isNotNull())
				.value(k -> assertThat(k.getGroupName()).isEqualTo("Newly"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("Created group"));

	}

	@Test
	public void create_whenAlreadyExists_expect4xx() {

		DayCareGroup group = new DayCareGroup(null, "Newly", "Created");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(g -> assertThat(g.getId()).isNotNull())
				.value(g -> assertThat(g.getGroupName()).isEqualTo("Newly"))
				.value(g -> assertThat(g.getGroupDescription()).isEqualTo("Created")).returnResult().getResponseBody();

		webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().is4xxClientError();

	}

	@Test
	public void update_shouldUpdate() {
		DayCareGroup group = webTestClient.get().uri(RESOURCE_PATH + existingDayCareGroup.getId()).exchange()
				.returnResult(DayCareGroup.class).getResponseBody().blockFirst();

		webTestClient.put().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isOk()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isEqualTo(group.getId()));
	}

	@Test
	public void update_whenNotExists_shouldReturn4xx() {
		DayCareGroup group = webTestClient.get().uri(RESOURCE_PATH + existingDayCareGroup.getId()).exchange()
				.returnResult(DayCareGroup.class).getResponseBody().blockFirst();
		group.setId(NOT_EXISTS_ID);
		webTestClient.put().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().is4xxClientError();
	}

	@Test
	public void delete_whenNotExists_shouldReturn4xx() {
		webTestClient.delete().uri(RESOURCE_PATH + NOT_EXISTS_ID).exchange().expectStatus().is4xxClientError();
	}

	@Test
	public void delete_shouldDelete() {

		// create
		DayCareGroup group = new DayCareGroup(null, "Newly", "Created");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(g -> assertThat(g.getId()).isNotNull())
				.value(g -> assertThat(g.getGroupName()).isEqualTo("Newly"))
				.value(g -> assertThat(g.getGroupDescription()).isEqualTo("Created")).returnResult().getResponseBody();

		webTestClient.get().uri(RESOURCE_PATH + group.getId()).exchange().expectStatus().isOk();

		webTestClient.delete().uri(RESOURCE_PATH + group.getId()).exchange().expectStatus().isOk();

		webTestClient.get().uri(RESOURCE_PATH + group.getId()).exchange().expectStatus().is4xxClientError();

	}

	@Test
	public void getGroupKids_shouldReturnAll() {

		webTestClient.get().uri(RESOURCE_PATH + "/" + existingDayCareGroup.getId() + "/kids").exchange().expectStatus()
				.isOk().expectBodyList(Kid.class).hasSize(groupKids.size())
				.consumeWith(body -> System.out.println(body));
	}

	@Test
	public void getGroupEmployees_shouldReturnAll() {

		webTestClient.get().uri(RESOURCE_PATH + "/" + existingDayCareGroup.getId() + "/employees").exchange()
				.expectStatus().isOk().expectBodyList(Employee.class).hasSize(groupEmployees.size())
				.consumeWith(body -> System.out.println(body));
	}

	@Test
	public void create_shouldCreateWithKidsAndEmployees() {

		DayCareGroup group = new DayCareGroup(null, "Newly with kids and tutors", "Created group with kids and tutors");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isNotNull())
				.value(k -> assertThat(k.getGroupName()).isEqualTo("Newly with kids and tutors"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("Created group with kids and tutors"))
				.returnResult().getResponseBody();

		// TODO Create kid instead of reusing previous one..
		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/kids/" + groupKids.get(0).getId()).contentType(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isCreated();

		// TODO Create tutor instead of reusing previous one..
		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/employees/" + groupEmployees.get(0).getId()).contentType(MediaType.APPLICATION_JSON).exchange(
				).expectStatus().isCreated();

		// Assert association created

		webTestClient.get().uri(RESOURCE_PATH + "/" + group.getId() + "/kids").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()				
				.expectBodyList(Kid.class).hasSize(1)
				.value(k -> assertThat(k.get(0).getId()).isEqualTo(groupKids.get(0).getId()));

		webTestClient.get().uri(RESOURCE_PATH + "/" + group.getId() + "/employees").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
				.expectBodyList(Tutor.class).hasSize(1)
				.value(t -> assertThat(t.get(0).getId()).isEqualTo(groupEmployees.get(0).getId()));

	}

	@Test
	public void deleteKidAssociation_shouldReturn200() {

		DayCareGroup group = new DayCareGroup(null, "Newly with kids and tutors", "Created group with kids and tutors");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isNotNull())
				.value(k -> assertThat(k.getGroupName()).isEqualTo("Newly with kids and tutors"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("Created group with kids and tutors"))
				.returnResult().getResponseBody();

		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/kids/" + groupKids.get(0).getId()).exchange()
				.expectStatus().isCreated();

		webTestClient.delete().uri(RESOURCE_PATH + "/" + group.getId() + "/kids/" + groupKids.get(0).getId()).exchange()
			.expectStatus().isOk();
		
		webTestClient.get().uri(RESOURCE_PATH + "/" + group.getId() + "/kids/").exchange()
			.expectStatus().isOk()
			.expectBodyList(Kid.class).hasSize(0);
	
	}
	
	@Test
	public void deleteEmployeeAssociation_shouldReturn200() {

		DayCareGroup group = new DayCareGroup(null, "Newly with kids and tutors", "Created group with kids and tutors");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isNotNull())
				.value(k -> assertThat(k.getGroupName()).isEqualTo("Newly with kids and tutors"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("Created group with kids and tutors"))
				.returnResult().getResponseBody();

		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/employees/" + groupEmployees.get(0).getId())
				.exchange().expectStatus().isCreated();


		webTestClient.delete().uri(RESOURCE_PATH + "/" + group.getId() + "/employees/" + groupEmployees.get(0).getId())
				.exchange().expectStatus().isOk();

		webTestClient.delete().uri(RESOURCE_PATH + "/" + group.getId() + "/employees/" + groupEmployees.get(0).getId())
			.exchange().expectStatus().isOk()
			.expectBodyList(Employee.class).hasSize(0);


	}
	
	
	@Test
	public void associatEmployeeTwice_shouldReturn4xx() {

		DayCareGroup group = new DayCareGroup(null, "Newly with kids and tutors", "Created group with kids and tutors");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isNotNull())
				.value(k -> assertThat(k.getGroupName()).isEqualTo("Newly with kids and tutors"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("Created group with kids and tutors"))
				.returnResult().getResponseBody();

		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/employees/" + groupEmployees.get(0).getId())
				.exchange().expectStatus().isCreated();

		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/employees/" + groupEmployees.get(0).getId())
				.exchange().expectStatus().is4xxClientError()
				.expectBody(ErrorResponse.class)
				.value(e -> assertThat(e.getMessage()).contains("The employee").contains(" is already associated with the group "))
				.consumeWith(e -> System.out.println(new String(e.getResponseBodyContent())));

	}

	
	@Test
	public void associateKidTwice_shouldReturn4xx() {

		//For question: identify a problem and how did you resolve this. Talk about this specific scenario.
		//associate twice made the service crash and return http500 when it should have been gracefully handled returning http400
		//Also talk about how I had to figure out how to implement proper exception handling that could be applied to all domain exceptions (RepositoryException)
		DayCareGroup group = new DayCareGroup(null, "Newly with kids and tutors", "Created group with kids and tutors");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isNotNull())
				.value(k -> assertThat(k.getGroupName()).isEqualTo("Newly with kids and tutors"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("Created group with kids and tutors"))
				.returnResult().getResponseBody();

		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/kids/" + groupKids.get(0).getId()).exchange()
				.expectStatus().isCreated();

		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/kids/" + groupKids.get(0).getId()).exchange()
				.expectStatus().is4xxClientError()
				.expectBody(ErrorResponse.class)
				.value(e -> assertThat(e.getMessage()).contains("The kid").contains(" is already associated with the group "))
				.consumeWith(e -> System.out.println(new String(e.getResponseBodyContent())));

	}

	@Test
	public void associateKid_GroupDoesNotExists_shouldReturn4xx() {

		//Another exemple of bug corrected. Was getting "group is already associated with.. error"
		webTestClient.put().uri(RESOURCE_PATH + "/" + Long.MAX_VALUE + "/kids/" + groupKids.get(0).getId()).exchange()
				.expectStatus().is4xxClientError()
				.expectBody(ErrorResponse.class)
				.value(e -> assertThat(e.getMessage()).contains("The group").contains("doesn't exists"))
				.consumeWith(e -> System.out.println(new String(e.getResponseBodyContent())));

	}

	@Test
	public void associateKid_kidDoesntExists_shouldReturn4xx() {

		//For question: identify a problem and how did you resolve this. Talk about this specific scenario.
		//associate twice made the service crash and return http500 when it should have been gracefully handled returning http400
		//Also talk about how I had to figure out how to implement proper exception handling that could be applied to all domain exceptions (RepositoryException)
		DayCareGroup group = new DayCareGroup(null, "Newly with kids and tutors", "Created group with kids and tutors");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isNotNull())
				.value(k -> assertThat(k.getGroupName()).isEqualTo("Newly with kids and tutors"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("Created group with kids and tutors"))
				.returnResult().getResponseBody();


		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/kids/" + Long.MAX_VALUE).exchange()
				.expectStatus().is4xxClientError()
				.expectBody(ErrorResponse.class)
				.value(e -> assertThat(e.getMessage()).contains("The kid").contains("doesn't exists"))
				.consumeWith(e -> System.out.println(new String(e.getResponseBodyContent())));

	}

	@Test
	public void associateEmployee_GroupDoesNotExists_shouldReturn4xx() {

		//Another exemple of bug corrected. Was getting "group is already associated with.. error"
		webTestClient.put().uri(RESOURCE_PATH + "/" + Long.MAX_VALUE + "/employees/" + groupEmployees.get(0).getId()).exchange()
				.expectStatus().is4xxClientError()
				.expectBody(ErrorResponse.class)
				.value(e -> assertThat(e.getMessage()).contains("The group").contains("doesn't exists"))
				.consumeWith(e -> System.out.println(new String(e.getResponseBodyContent())));

	}
	
	@Test
	public void associatEmployee_employeeDoesntExists_shouldReturn4xx() {

		DayCareGroup group = new DayCareGroup(null, "Newly with kids and tutors", "Created group with kids and tutors");

		group = webTestClient.post().uri(RESOURCE_PATH).bodyValue(group).exchange().expectStatus().isCreated()
				.expectBody(DayCareGroup.class).value(k -> assertThat(k.getId()).isNotNull())
				.value(k -> assertThat(k.getGroupName()).isEqualTo("Newly with kids and tutors"))
				.value(k -> assertThat(k.getGroupDescription()).isEqualTo("Created group with kids and tutors"))
				.returnResult().getResponseBody();

		webTestClient.put().uri(RESOURCE_PATH + "/" + group.getId() + "/employees/" + Long.MAX_VALUE)
				.exchange().expectStatus().is4xxClientError()
				.expectBody(ErrorResponse.class)
				.value(e -> assertThat(e.getMessage()).contains("The employee").contains("doesn't exists"))
				.consumeWith(e -> System.out.println(new String(e.getResponseBodyContent())));

	}

}
