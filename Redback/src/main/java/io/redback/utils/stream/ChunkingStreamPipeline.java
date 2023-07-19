package io.redback.utils.stream;

import java.util.ArrayList;
import java.util.List;

import io.firebus.logging.Logger;

public class ChunkingStreamPipeline<TARGET, SOURCE> implements DataStreamNextHandler {
	protected List<TARGET> targetBuffer;
	protected List<SOURCE> sourceBuffer;
	protected DataStream<TARGET> targetStream;
	protected DataStream<SOURCE> sourceStream;
	protected ChunkingConverter<TARGET, SOURCE> converter;
	protected int chunkSize;
	//protected int nextBacklog = 1;
	protected boolean inStreamComplete = false;
	
	public ChunkingStreamPipeline(DataStream<TARGET> ts, int cs, ChunkingConverter<TARGET, SOURCE> cc) {
		targetStream = ts;
		chunkSize = cs;
		converter = cc;
		sourceBuffer = new ArrayList<SOURCE>();
		targetBuffer = new ArrayList<TARGET>();
		
		sourceStream = new DataStream<SOURCE>() {
			protected void received(SOURCE data) {
				synchronized(sourceBuffer) {
					sourceBuffer.add(data);
				}
				convertOrRequestNext();
			}

			protected void completed() {
				inStreamComplete = true;
				if(sourceBuffer.size() > 0)
					convert();
			}
		};
		
		targetStream.setNextHandler(this);
	}
	
	protected void convertOrRequestNext() {
		if(sourceBuffer.size() >= chunkSize || inStreamComplete) {
			convert();
		} else {
			sourceStream.requestNext();
		}
	}
	
	protected void convert() {
		try {
			List<SOURCE> list = new ArrayList<SOURCE>();
			synchronized(sourceBuffer) {
				while(list.size() < chunkSize && sourceBuffer.size() > 0) {
					list.add(sourceBuffer.remove(0));
				}
			}
			List<TARGET> outList = converter.convert(list);
			targetBuffer.addAll(outList);
			sendNext();
		} catch (Exception e) {
			Logger.severe("rb.sendingstreampipeline.send", e);
		}
	}

	public void sendNext() {
		if(targetBuffer.size() > 0) {
			targetStream.send(targetBuffer.remove(0));
		} else {
			sourceStream.requestNext();
		}	
	}
	
	public DataStream<SOURCE> getSourceDataStream() {
		return sourceStream;
	}
}
