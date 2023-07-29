package io.redback.utils.stream;

import java.util.ArrayList;
import java.util.List;

import io.firebus.logging.Logger;

public class ChunkConverterStreamPipeline<TARGET, SOURCE> implements DataStreamNextHandler {
	protected List<TARGET> targetBuffer;
	protected List<SOURCE> sourceBuffer;
	protected DataStream<TARGET> targetStream;
	protected DataStream<SOURCE> sourceStream;
	protected ChunkConverter<TARGET, SOURCE> converter;
	protected int chunkSize;
	protected boolean sourceStreamComplete = false;
	
	public ChunkConverterStreamPipeline(DataStream<TARGET> ts, int cs, ChunkConverter<TARGET, SOURCE> cc) {
		//System.out.println("CSP Created");
		targetStream = ts;
		chunkSize = cs > 0 ? cs : 50;
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
				sourceStreamComplete = true;
				convertOrRequestNext();
			}
		};
		
		targetStream.setNextHandler(this);
	}
	
	protected void convertOrRequestNext() {
		if((sourceBuffer.size() >= chunkSize || sourceStreamComplete)) {
			//System.out.println("CSP converting, source=" + sourceBuffer.size());
			convert();
		} else if(!sourceStreamComplete) {
			//System.out.println("CSP request next");
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
		//System.out.println("CSP send next, target=" + targetBuffer.size() + " sourceComplete=" + sourceStreamComplete);
		if(targetBuffer.size() > 0) {
			targetStream.send(targetBuffer.remove(0));
		} else if(sourceStreamComplete == false) {
			sourceStream.requestNext();			
		} else {
			targetStream.complete();
		}	
	}
	
	public DataStream<SOURCE> getSourceDataStream() {
		return sourceStream;
	}
}
