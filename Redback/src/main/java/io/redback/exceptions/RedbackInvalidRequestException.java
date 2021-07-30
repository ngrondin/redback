package io.redback.exceptions;

public class RedbackInvalidRequestException extends RedbackException {
	private static final long serialVersionUID = 358217727297167404L;

	public RedbackInvalidRequestException(String msg) {
		super(msg);
	}
	
	public RedbackInvalidRequestException(String msg, Throwable t) {
		super(msg, t);
	}

	public int getErrorCode() {
		return 400;
	}
}
