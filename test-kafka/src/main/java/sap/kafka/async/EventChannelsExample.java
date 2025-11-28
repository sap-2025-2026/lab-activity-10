package sap.kafka.async;

import java.util.logging.Logger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class EventChannelsExample {

	static Logger logger = Logger.getLogger("[Controller]");
		
	public static void main(String[] args) throws Exception {
		var vertx = Vertx.vertx();

		var channelName = "my-event-channel";
		var location = "localhost:29092";
		
		var admin = new EventChannelsAdmin(vertx, location);
		admin.purgeChannel(channelName);
		
		InputEventChannel inputEvCh = new InputEventChannel(vertx, channelName, location);
		inputEvCh.init((JsonObject ev) -> {
			logger.info("new event: " + ev.encodePrettily());
		}).onSuccess(r -> {					
			OutputEventChannel outputEvCh = new OutputEventChannel(vertx, channelName, location);
			JsonObject ev = new JsonObject();
			ev.put("eventId", 1);
			ev.put("eventType", "something-happened");
			outputEvCh
				.postEvent(ev)
				.onSuccess(v -> {
					logger.info("posted");
				});
		
			JsonObject ev2 = new JsonObject();
			ev2.put("eventId", 2);
			ev2.put("eventType", "something-happened-again");
			outputEvCh
				.postEvent(ev2)
				.onSuccess(v -> {
					logger.info("posted");
				});
		}).onFailure(f -> {
			logger.info("Error: " + f);
		});
					
		Thread.sleep(100000);
			
	}
}
