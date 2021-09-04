package io.redback.exceptions;

public class RedbackUnauthorisedException extends RedbackException {
	private static final long serialVersionUID = 358217727297167404L;

	public RedbackUnauthorisedException(String msg) {
		super(msg);
	}
	
	public RedbackUnauthorisedException(String msg, Throwable t) {
		super(msg, t);
	}

	public int getErrorCode() {
		return 401;
	}
}
