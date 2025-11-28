package sap.kafka.sync;

import java.util.logging.Logger;

import org.json.JSONObject;

class MyAgent extends Thread {
	static Logger logger = Logger.getLogger("[Consumer]");

	private InputEventChannel chEvIn;
	
	MyAgent(InputEventChannel chEvIn){
		this.chEvIn = chEvIn;
	}
	
	public void run() {
		logger.info("waiting for events.");
		while (true) {
			var ev = chEvIn.waitForEvent();
			logger.info("new event: " + ev.toString());
		}
		
	}
}

public class EventChannelsExample {

	static Logger logger = Logger.getLogger("[Main]");
		
	public static void main(String[] args) throws Exception {
		var channelName = "my-event-channel";
		var location = "localhost:9092";
		
		var admin = new EventChannelsAdmin(location);

		admin.purgeChannel(channelName);
		
		InputEventChannel chEvIn = new InputEventChannel(channelName, location);	
		OutputEventChannel chEvOut = new OutputEventChannel(channelName, location);
		
		new MyAgent(chEvIn).start();
		
		logger.info("posting events...");
		JSONObject ev1 = new JSONObject();
		ev1.put("eventId", 1);
		ev1.put("eventType", "something-happened");
		chEvOut.postEvent(ev1);
		logger.info("Event 1 posted");

		JSONObject ev2 = new JSONObject();
		ev2.put("eventId", 2);
		ev2.put("eventType", "something-happened-again");
		chEvOut.postEvent(ev2);
		logger.info("Event 2 posted");
	}
}
