package org.alfresco.museum.ucm.sizelimits;

public class SiteSizeLimitExceededException extends RuntimeException {
	public SiteSizeLimitExceededException() {
		super();
	}

	public SiteSizeLimitExceededException(String message) {
		super(message);
	}
}
