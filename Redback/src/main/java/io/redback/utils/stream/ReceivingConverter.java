package io.redback.utils.stream;

import java.util.List;

import io.firebus.Payload;
import io.firebus.data.DataException;

public interface ReceivingConverter<T> {
	public List<T> convert(Payload payload) throws DataException;
}
