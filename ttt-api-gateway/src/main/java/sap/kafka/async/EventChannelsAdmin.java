package sap.kafka.async;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClientConfig;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;

public class EventChannelsAdmin {

	private KafkaAdminClient adminClient;
	private Vertx vertx;
	
	public EventChannelsAdmin(Vertx vertx, String address) {
		this.vertx = vertx;
		Properties config = new Properties();
		config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, address);
		adminClient = KafkaAdminClient.create(vertx, config);	
	}
	
	public Future<Object> purgeChannel(String channelName) {
		var prom = Promise.promise();
		adminClient.deleteTopics(Collections.singletonList(channelName))
		  .onSuccess(v -> {
			  // topics deleted successfully
			  prom.succeed();
		  })
		  .onFailure(cause -> {
		    // something went wrong when removing the topics
			  prom.fail(cause.getMessage());
		  });
		return prom.future();
	}
	
}
