package org.opendaycare.repositoryservice.service.kid;

import org.opendaycare.repositoryservice.model.Kid;
import org.opendaycare.repositoryservice.repository.KidsRepository;
import org.opendaycare.repositoryservice.service.AbstractRepositoryService;
import org.springframework.stereotype.Service;

@Service
public class KidsServiceImpl extends AbstractRepositoryService<Kid, Long> implements KidsService {

	public KidsServiceImpl(KidsRepository repository) {
		super(repository, "Kid");
	}
	
}
