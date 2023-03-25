package io.redback.exceptions;

public class RedbackConfigNotFoundException extends RedbackException {
	private static final long serialVersionUID = 358217727297167404L;

	public RedbackConfigNotFoundException(String msg) {
		super(msg);
	}
	
	public RedbackConfigNotFoundException(String msg, Throwable t) {
		super(msg, t);
	}

	public int getErrorCode() {
		return 500;
	}
}
