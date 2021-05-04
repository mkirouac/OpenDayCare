package org.opendaycare.repositoryservice.service.exception;

public class RepositoryServiceException extends RuntimeException {

	public RepositoryServiceException() {
		super();
	}

	public RepositoryServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RepositoryServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public RepositoryServiceException(String message) {
		super(message);
	}

	public RepositoryServiceException(Throwable cause) {
		super(cause);
	}

}
