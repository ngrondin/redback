package io.redback.exceptions;

public class RedbackResourceNotFoundException extends RedbackException {
	private static final long serialVersionUID = 358217727297167404L;

	public RedbackResourceNotFoundException(String msg) {
		super(msg);
	}
	
	public RedbackResourceNotFoundException(String msg, Throwable t) {
		super(msg, t);
	}

	public int getErrorCode() {
		return 404;
	}
}
