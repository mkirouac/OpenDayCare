package org.opendaycare.repositoryservice.rest;

import org.opendaycare.repositoryservice.model.Tutor;
import org.opendaycare.repositoryservice.service.tutor.TutorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/tutor")
public class TutorRestController extends AbstractRestController<Tutor, Long> {

	public TutorRestController(TutorService service) {
		super(service);
	}
	
}
