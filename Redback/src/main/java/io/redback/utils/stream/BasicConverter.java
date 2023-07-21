package io.redback.utils.stream;

import io.firebus.data.DataException;
import io.redback.exceptions.RedbackException;

public interface BasicConverter<OUT, IN> {
	public OUT convert(IN item) throws DataException, RedbackException;
}
