package org.opendaycare.repositoryservice.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendaycare.repositoryservice.model.Employee;
import org.opendaycare.repositoryservice.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class EmployeeRestControllerTest {

	private static final String RESOURCE_PATH = "/employee/";


	private static final Long NOT_EXISTS_ID = 9999999L;
	

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    private Employee existingEmployee;
    
    @BeforeEach 
    public void setUp() {
    	existingEmployee = employeeRepository.save(new Employee(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
    }
    
	@Test
	public void getAll_shouldReturnAll() {
		webTestClient.get().uri(RESOURCE_PATH).exchange()
			.expectStatus().isOk()
			.expectBodyList(Employee.class)
			.contains(existingEmployee)
			.consumeWith(response -> {
				List<Employee> employees = response.getResponseBody();
				employees.forEach(k -> {
					assertThat(k.getId()).isNotNull();
				});
			});
			
	}
	
	@Test
	public void getOneById_shouldReturnOne() {
		webTestClient.get().uri(RESOURCE_PATH + existingEmployee.getId()).exchange()
			.expectStatus().isOk()
			.expectBody(Employee.class)
			.value(k -> assertThat(k.getId()).isEqualTo(existingEmployee.getId()))
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Jon"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Doe"));
	}
	
	@Test
	public void getOneById_shouldReturn404_whenNotFound() {
		webTestClient.get().uri(RESOURCE_PATH + NOT_EXISTS_ID).exchange()
			.expectStatus().isNotFound();
	}
	
	@Test
	public void create_shouldCreateOne() {
		
		Employee employee = new Employee(null, "Newly", "Created", LocalDate.MIN);
		
		webTestClient.post().uri(RESOURCE_PATH).bodyValue(employee).exchange()
			.expectStatus().isCreated()
			.expectBody(Employee.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"));
		
		
	}
	
	
	@Test
	public void create_whenAlreadyExists_expect4xx() {
		
		Employee employee = new Employee(null, "Newly", "Created", LocalDate.MIN);
		
		employee = webTestClient.post().uri(RESOURCE_PATH).bodyValue(employee).exchange()
			.expectStatus().isCreated()
			.expectBody(Employee.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"))
			.returnResult().getResponseBody();
		
		
		webTestClient.post().uri(RESOURCE_PATH).bodyValue(employee).exchange()
		.expectStatus().is4xxClientError();
		
	}
	
	@Test
	public void update_shouldUpdate() {
		Employee employee = webTestClient.get().uri(RESOURCE_PATH+ existingEmployee.getId()).exchange().returnResult(Employee.class).getResponseBody().blockFirst();
		
		webTestClient.put().uri(RESOURCE_PATH).bodyValue(employee).exchange()
			.expectStatus().isOk()
			.expectBody(Employee.class)
			.value(k -> assertThat(k.getId()).isEqualTo(employee.getId()));
	}
	
	@Test
	public void update_whenNotExists_shouldReturn4xx() {
		Employee employee = webTestClient.get().uri(RESOURCE_PATH + existingEmployee.getId()).exchange().returnResult(Employee.class).getResponseBody().blockFirst();
		employee.setId(NOT_EXISTS_ID);
		webTestClient.put().uri(RESOURCE_PATH).bodyValue(employee).exchange()
			.expectStatus().is4xxClientError();
	}
	
	@Test
	public void delete_whenNotExists_shouldReturn4xx() {
		webTestClient.delete().uri(RESOURCE_PATH + NOT_EXISTS_ID)
			.exchange()
			.expectStatus().is4xxClientError();
	}
	
	@Test
	public void delete_shouldDelete() {
		
		//create
		Employee employee = new Employee(null, "Newly", "Created", LocalDate.MIN);
		
		employee = webTestClient.post().uri(RESOURCE_PATH).bodyValue(employee).exchange()
			.expectStatus().isCreated()
			.expectBody(Employee.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"))
			.returnResult().getResponseBody();
		
		webTestClient.get().uri(RESOURCE_PATH + employee.getId()).exchange()
			.expectStatus().isOk();
		
		webTestClient.delete().uri(RESOURCE_PATH + employee.getId()).exchange()
			.expectStatus().isOk();
		
		webTestClient.get().uri(RESOURCE_PATH + employee.getId()).exchange()
			.expectStatus().is4xxClientError();
		
		
	}
	
}
