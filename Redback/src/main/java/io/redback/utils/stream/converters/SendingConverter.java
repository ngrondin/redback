package io.redback.utils.stream.converters;

import java.util.List;

import io.firebus.Payload;
import io.firebus.data.DataException;
import io.redback.exceptions.RedbackException;

public interface SendingConverter<T> {
	public Payload convert(List<T> list) throws DataException, RedbackException;
}
