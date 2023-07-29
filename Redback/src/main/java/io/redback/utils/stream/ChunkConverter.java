package io.redback.utils.stream;

import java.util.List;

import io.firebus.data.DataException;
import io.redback.exceptions.RedbackException;

public interface ChunkConverter<OUT, IN> {
	public List<OUT> convert(List<IN> list) throws DataException, RedbackException;
}
