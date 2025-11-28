package sap.kafka.sync;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONObject;

public class InputEventChannel {
	static Logger logger = Logger.getLogger("[InputEventChannel]");

	private String name;
	private KafkaConsumer<String, String> consumer;
	
	public InputEventChannel(String name, String address) {
		this.name = name;

	      Properties config = new Properties();
	      config.put("bootstrap.servers", address);
	      config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	      config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	      config.put("max.poll.records", "1");
	      config.put("group.id", "test");
	      config.put("auto.commit.interval.ms", "1000");
	      config.put("session.timeout.ms", "30000");
	      config.put("auto.offset.reset", "earliest");
	      config.put("enable.auto.commit", "true");
		
	      consumer = new KafkaConsumer<String, String>(config);
		  consumer.subscribe(Arrays.asList(name));
		
	}
	
	public JSONObject waitForEvent() {
		ConsumerRecords<String, String> records = null;
		do {
			records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
			log("records " + records.count());
		} while (records.isEmpty());
		var record = records.iterator().next();
		log("Processing key=" + record.key() + ",value=" + record.value() +
				    ",partition=" + record.partition() + ",offset=" + record.offset());
		return new JSONObject(record.value());	  
	}

	private void log(String msg) {
		logger.info("["+name+"] " + msg); 
	}
	
}
