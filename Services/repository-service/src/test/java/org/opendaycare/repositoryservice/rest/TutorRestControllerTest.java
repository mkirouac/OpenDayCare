package org.opendaycare.repositoryservice.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendaycare.repositoryservice.model.Tutor;
import org.opendaycare.repositoryservice.repository.TutorRepository;
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
public class TutorRestControllerTest {

	private static final String RESOURCE_PATH = "/tutor/";


	private static final Long NOT_EXISTS_ID = 9999999L;
	

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private TutorRepository tutorRepository;
    
    private Tutor existingTutor;
    
    @BeforeEach 
    public void setUp() {
    	existingTutor = tutorRepository.save(new Tutor(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
    }
    
	@Test
	public void getAll_shouldReturnAll() {
		webTestClient.get().uri(RESOURCE_PATH).exchange()
			.expectStatus().isOk()
			.expectBodyList(Tutor.class)
			.contains(existingTutor)
			.consumeWith(response -> {
				List<Tutor> tutors = response.getResponseBody();
				tutors.forEach(k -> {
					assertThat(k.getId()).isNotNull();
				});
			});
			
	}
	
	@Test
	public void getOneById_shouldReturnOne() {
		webTestClient.get().uri(RESOURCE_PATH + existingTutor.getId()).exchange()
			.expectStatus().isOk()
			.expectBody(Tutor.class)
			.value(k -> assertThat(k.getId()).isEqualTo(existingTutor.getId()))
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
		
		Tutor tutor = new Tutor(null, "Newly", "Created", LocalDate.MIN);
		
		webTestClient.post().uri(RESOURCE_PATH).bodyValue(tutor).exchange()
			.expectStatus().isCreated()
			.expectBody(Tutor.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"));
		
		
	}
	
	
	@Test
	public void create_whenAlreadyExists_expect4xx() {
		
		Tutor tutor = new Tutor(null, "Newly", "Created", LocalDate.MIN);
		
		tutor = webTestClient.post().uri(RESOURCE_PATH).bodyValue(tutor).exchange()
			.expectStatus().isCreated()
			.expectBody(Tutor.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"))
			.returnResult().getResponseBody();
		
		
		webTestClient.post().uri(RESOURCE_PATH).bodyValue(tutor).exchange()
		.expectStatus().is4xxClientError();
		
	}
	
	@Test
	public void update_shouldUpdate() {
		Tutor tutor = webTestClient.get().uri(RESOURCE_PATH+ existingTutor.getId()).exchange().returnResult(Tutor.class).getResponseBody().blockFirst();
		
		webTestClient.put().uri(RESOURCE_PATH).bodyValue(tutor).exchange()
			.expectStatus().isOk()
			.expectBody(Tutor.class)
			.value(k -> assertThat(k.getId()).isEqualTo(tutor.getId()));
	}
	
	@Test
	public void update_whenNotExists_shouldReturn4xx() {
		Tutor tutor = webTestClient.get().uri(RESOURCE_PATH + existingTutor.getId()).exchange().returnResult(Tutor.class).getResponseBody().blockFirst();
		tutor.setId(NOT_EXISTS_ID);
		webTestClient.put().uri(RESOURCE_PATH).bodyValue(tutor).exchange()
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
		Tutor tutor = new Tutor(null, "Newly", "Created", LocalDate.MIN);
		
		tutor = webTestClient.post().uri(RESOURCE_PATH).bodyValue(tutor).exchange()
			.expectStatus().isCreated()
			.expectBody(Tutor.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"))
			.returnResult().getResponseBody();
		
		webTestClient.get().uri(RESOURCE_PATH + tutor.getId()).exchange()
			.expectStatus().isOk();
		
		webTestClient.delete().uri(RESOURCE_PATH + tutor.getId()).exchange()
			.expectStatus().isOk();
		
		webTestClient.get().uri(RESOURCE_PATH + tutor.getId()).exchange()
			.expectStatus().is4xxClientError();
		
		
	}
	
}
