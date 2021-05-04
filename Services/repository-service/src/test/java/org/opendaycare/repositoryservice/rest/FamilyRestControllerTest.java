package org.opendaycare.repositoryservice.rest;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.model.Tutor;
import org.opendaycare.repositoryservice.repository.KidsRepository;
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
public class FamilyRestControllerTest {


	private static final String RESOURCE_PATH = "/family/";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private KidsRepository kidsRepository;
	
    @Autowired
    private TutorRepository tutorRepository;
    
    
	@Test
	public void itShouldAssociateKidToTutor() {
		Tutor tutor = tutorRepository.save(new Tutor(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		Kid kid = kidsRepository.save(new Kid(null, "kido", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		webTestClient.put().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid/" + kid.getId()).exchange()
			.expectStatus().isCreated();
	}
	
	@Test
	public void itShouldAssociateTutorToKid() {
		Kid kid = kidsRepository.save(new Kid(null, "kido", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		Tutor tutor = tutorRepository.save(new Tutor(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		webTestClient.put().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor/" + tutor.getId()).exchange()
			.expectStatus().isCreated();
	}
	
	@Test
	public void itShouldUnassociateKidFromTutor() {

		Tutor tutor = tutorRepository.save(new Tutor(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		Kid kid1 = kidsRepository.save(new Kid(null, "kido", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		Kid kid2 = kidsRepository.save(new Kid(null, "kida", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		webTestClient.put().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid/" + kid1.getId()).exchange()
			.expectStatus().isCreated();
		webTestClient.put().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid/" + kid2.getId()).exchange()
			.expectStatus().isCreated();
		
		
		webTestClient.get().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid").exchange()
			.expectStatus().isOk()
			.expectBodyList(Kid.class).hasSize(2);
		
		webTestClient.delete().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid/" + kid1.getId()).exchange()
			.expectStatus().isOk();
		

		webTestClient.get().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid").exchange()
			.expectStatus().isOk()
			.expectBodyList(Kid.class).hasSize(1);
		
	}
	
	@Test
	public void itShouldUnassociateTutorFromKid() {
		Kid kid = kidsRepository.save(new Kid(null, "kido", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		Tutor tutor1 = tutorRepository.save(new Tutor(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		Tutor tutor2 = tutorRepository.save(new Tutor(null, "Jane", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		webTestClient.put().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor/" + tutor1.getId()).exchange()
			.expectStatus().isCreated();
		webTestClient.put().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor/" + tutor2.getId()).exchange()
			.expectStatus().isCreated();
		
		
		webTestClient.get().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor").exchange()
			.expectStatus().isOk()
			.expectBodyList(Tutor.class).hasSize(2);
		
		webTestClient.delete().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor/" + tutor1.getId()).exchange()
			.expectStatus().isOk();
		
		webTestClient.get().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor").exchange()
			.expectStatus().isOk()
			.expectBodyList(Tutor.class).hasSize(1);
	}
	
	@Test
	public void itShouldRetrieveKidTutors() {
		Kid kid = kidsRepository.save(new Kid(null, "kido", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		Tutor tutor1 = tutorRepository.save(new Tutor(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		Tutor tutor2 = tutorRepository.save(new Tutor(null, "Jane", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		webTestClient.put().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor/" + tutor1.getId()).exchange()
			.expectStatus().isCreated();
		webTestClient.put().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor/" + tutor2.getId()).exchange()
			.expectStatus().isCreated();
		
		
		webTestClient.get().uri(RESOURCE_PATH + "/kid/" + kid.getId() + "/tutor").exchange()
			.expectStatus().isOk()
			.expectBodyList(Tutor.class).hasSize(2);
			
		
	}
	
	@Test
	public void itShouldRetrieveTutorKids() {
		
		Tutor tutor = tutorRepository.save(new Tutor(null, "Jon", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		Kid kid1 = kidsRepository.save(new Kid(null, "kido", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		Kid kid2 = kidsRepository.save(new Kid(null, "kida", "Doe", LocalDate.of(2021, 01, 01))).log().block();
		
		webTestClient.put().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid/" + kid1.getId()).exchange()
			.expectStatus().isCreated();
		webTestClient.put().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid/" + kid2.getId()).exchange()
			.expectStatus().isCreated();
		
		
		webTestClient.get().uri(RESOURCE_PATH + "/tutor/" + tutor.getId() + "/kid").exchange()
			.expectStatus().isOk()
			.expectBodyList(Kid.class).hasSize(2);
	}
	
	
}
