package org.opendaycare.repositoryservice.service.exception;

public class RepositoryServiceNonRetryableException extends RepositoryServiceException {

	public RepositoryServiceNonRetryableException() {
	}

	public RepositoryServiceNonRetryableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RepositoryServiceNonRetryableException(String message, Throwable cause) {
		super(message, cause);
	}

	public RepositoryServiceNonRetryableException(String message) {
		super(message);
	}

	public RepositoryServiceNonRetryableException(Throwable cause) {
		super(cause);
	}

}
