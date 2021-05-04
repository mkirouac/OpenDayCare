package org.opendaycare.repositoryservice.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.repository.KidsRepository;
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
public class KidsRestControllerTest {

	private static final String RESOURCE_PATH = "/kid/";


	private static final Long NOT_EXISTS_ID = 9999999L;
	

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private KidsRepository kidsRepository;
    
    private Kid existingKid;
    
    @BeforeEach 
    public void setUp() {
    	existingKid = kidsRepository.save(new Kid(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
    	System.out.println("Setup Completed");
    }
    
	@Test
	public void getAllKids_shouldReturnAll() {
		System.out.println("Test Started itShouldGetAllKids");
		webTestClient.get().uri(RESOURCE_PATH).exchange()
			.expectStatus().isOk()
			.expectBodyList(Kid.class)
			.contains(existingKid)
			.consumeWith(kid -> {
				List<Kid> kids = kid.getResponseBody();
				kids.forEach(k -> {
					assertThat(k.getId()).isNotNull();
				});
			});
			
	}
	
	@Test
	public void getOneKidById_shouldReturnOne() {
		System.out.println("Test Started itShouldGetOneKidById");
		webTestClient.get().uri(RESOURCE_PATH + existingKid.getId()).exchange()
			.expectStatus().isOk()
			.expectBody(Kid.class)
			.value(k -> assertThat(k.getId()).isEqualTo(existingKid.getId()))
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Jon"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Doe"));
	}
	
	@Test
	public void getOneKidById_shouldReturn404_whenNotFound() {
		System.out.println("Test Started 404");
		webTestClient.get().uri(RESOURCE_PATH + NOT_EXISTS_ID).exchange()
			.expectStatus().isNotFound();
	}
	
	@Test
	public void createKid_shouldCreateOne() {
		
		Kid kid = new Kid(null, "Newly", "Created", LocalDate.MIN);
		
		webTestClient.post().uri(RESOURCE_PATH).bodyValue(kid).exchange()
			.expectStatus().isCreated()
			.expectBody(Kid.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"));
		
		
	}
	
	
	@Test
	public void createKid_whenAlreadyExists_expect4xx() {
		
		Kid kid = new Kid(null, "Newly", "Created", LocalDate.MIN);
		
		kid = webTestClient.post().uri(RESOURCE_PATH).bodyValue(kid).exchange()
			.expectStatus().isCreated()
			.expectBody(Kid.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"))
			.returnResult().getResponseBody();
		
		
		webTestClient.post().uri(RESOURCE_PATH).bodyValue(kid).exchange()
		.expectStatus().is4xxClientError();
		
	}
	
	@Test
	public void updateKid_shouldUpdate() {
		Kid kid = webTestClient.get().uri(RESOURCE_PATH + existingKid.getId()).exchange().returnResult(Kid.class).getResponseBody().blockFirst();
		
		webTestClient.put().uri(RESOURCE_PATH).bodyValue(kid).exchange()
			.expectStatus().isOk()
			.expectBody(Kid.class)
			.value(k -> assertThat(k.getId()).isEqualTo(kid.getId()));
	}
	
	@Test
	public void updateKid_whenNotExists_shouldReturn4xx() {
		Kid kid = webTestClient.get().uri(RESOURCE_PATH + existingKid.getId()).exchange().returnResult(Kid.class).getResponseBody().blockFirst();
		kid.setId(NOT_EXISTS_ID);
		webTestClient.put().uri(RESOURCE_PATH).bodyValue(kid).exchange()
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
		Kid kid = new Kid(null, "Newly", "Created", LocalDate.MIN);
		
		kid = webTestClient.post().uri(RESOURCE_PATH).bodyValue(kid).exchange()
			.expectStatus().isCreated()
			.expectBody(Kid.class)
			.value(k -> assertThat(k.getId()).isNotNull())
			.value(k -> assertThat(k.getFirstName()).isEqualTo("Newly"))
			.value(k -> assertThat(k.getLastName()).isEqualTo("Created"))
			.returnResult().getResponseBody();
		
		webTestClient.get().uri(RESOURCE_PATH + kid.getId()).exchange()
			.expectStatus().isOk();
		
		webTestClient.delete().uri(RESOURCE_PATH + kid.getId()).exchange()
			.expectStatus().isOk();
		
		webTestClient.get().uri(RESOURCE_PATH + kid.getId()).exchange()
			.expectStatus().is4xxClientError();
		
		
	}
	
}
