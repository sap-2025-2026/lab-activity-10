package sap.kafka.sync;

import java.util.Properties;
import java.util.logging.Logger;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;

public class OutputEventChannel {
	static Logger logger = Logger.getLogger("[OutputEventChannel]");

	private String name;
	private KafkaProducer<String, String> producer;
	
	public OutputEventChannel(String name, String address) {
		this.name = name;

	      Properties config = new Properties();
			config.put("bootstrap.servers", address);
			config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		    config.put("group.id", "test");
			config.put("acks", "1");
		
	      producer = new KafkaProducer<String, String>(config);		
	}
	
	public void postEvent(JSONObject ev) {
	    var rec = new ProducerRecord<String, String>(name, null, ev.toString());
		producer.send(rec);
	}

	private void log(String msg) {
		logger.info("["+name+"] " + msg); 
	}
	
}
