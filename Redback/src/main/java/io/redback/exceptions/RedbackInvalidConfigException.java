package io.redback.exceptions;

public class RedbackInvalidConfigException extends RedbackException {
	private static final long serialVersionUID = 358217727297167404L;

	public RedbackInvalidConfigException(String msg) {
		super(msg);
	}
	
	public RedbackInvalidConfigException(String msg, Throwable t) {
		super(msg, t);
	}

	public int getErrorCode() {
		return 500;
	}
}
