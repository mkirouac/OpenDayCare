package org.opendaycare.repositoryservice.service.tutor;

import org.opendaycare.repositoryservice.model.Tutor;
import org.opendaycare.repositoryservice.repository.TutorRepository;
import org.opendaycare.repositoryservice.service.AbstractRepositoryService;
import org.springframework.stereotype.Service;

@Service
public class TutorServiceImpl extends AbstractRepositoryService<Tutor, Long> implements TutorService {

	public TutorServiceImpl(TutorRepository repository) {
		super(repository, "Tutor");
	}

}
