package io.redback.utils.stream;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.StreamHandler;
import io.firebus.logging.Logger;

public class SendingStreamPipeline<T> implements StreamHandler {
	protected List<T> buffer;
	protected int chunkSize;
	protected DataStream<T> dataStream;
	protected StreamEndpoint sep;
	protected SendingConverter<T> converter;
	protected int nextBacklog = 1;
	protected boolean streamComplete = false;
	
	public SendingStreamPipeline(StreamEndpoint s, int cs, SendingConverter<T> rc) {
		sep = s;
		converter = rc;
		chunkSize = cs > 0 ? cs : 50;
		buffer = new ArrayList<T>();
		dataStream = new DataStream<T>() {
			protected void received(T data) {
				synchronized(buffer) {
					buffer.add(data);
				}
				determineAction();
			}

			protected void completed() {
				streamComplete = true;
				determineAction();
			}
		};
		sep.setHandler(this);
	}
	
	protected void determineAction() {
		if(nextBacklog > 0) {
			if(buffer.size() > 0 && (buffer.size() >= chunkSize || streamComplete)) {
				sendBuffer();
			} else if(!streamComplete) {
				dataStream.requestNext();			
			} else {
				sep.close();
			}			
		}
	}
	
	protected void sendBuffer() {
		try {
			List<T> list = new ArrayList<T>();
			synchronized(buffer) {
				while(list.size() < chunkSize && buffer.size() > 0)
					list.add(buffer.remove(0));
			}
			sep.send(converter.convert(list));
			if(nextBacklog > 0) nextBacklog--;				
		} catch (Exception e) {
			Logger.severe("rb.sendingstreampipeline.send", e);
		} 
	}

	public void receiveStreamData(Payload payload) {
		try {
			String s = payload.getString();
			if(s.equals("next")) {
				nextBacklog++;
				determineAction();
			}
		} catch(Exception e) {
			Logger.severe("rb.sendingstreampipeline.receive", e);
		}
	}
	
	public void streamClosed() {
	}
	
	public void streamError(FunctionErrorException error) {
		
	}
	
	public DataStream<T> getDataStream() {
		return dataStream;
	}

}
