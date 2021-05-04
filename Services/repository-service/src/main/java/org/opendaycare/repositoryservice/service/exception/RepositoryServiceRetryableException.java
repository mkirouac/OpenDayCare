package org.opendaycare.repositoryservice.service.exception;

public class RepositoryServiceRetryableException extends RepositoryServiceException {

	public RepositoryServiceRetryableException() {
	}

	public RepositoryServiceRetryableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RepositoryServiceRetryableException(String message, Throwable cause) {
		super(message, cause);
	}

	public RepositoryServiceRetryableException(String message) {
		super(message);
	}

	public RepositoryServiceRetryableException(Throwable cause) {
		super(cause);
	}

}
