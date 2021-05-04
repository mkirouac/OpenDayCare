package org.opendaycare.repositoryservice.rest;

import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.service.kid.KidsServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/kid")
public class KidsRestController extends AbstractRestController<Kid, Long> {

	public KidsRestController(KidsServiceImpl service) {
		super(service);
	}

	
}
